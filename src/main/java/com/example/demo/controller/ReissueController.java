package com.example.demo.controller;

import com.example.demo.entity.RefreshEntity;
import com.example.demo.jwt.JWTUtil;
import com.example.demo.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@ResponseBody
public class ReissueController {
  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;

  public ReissueController(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
    this.jwtUtil = jwtUtil;
    this.refreshRepository = refreshRepository;
  }

  @PostMapping({"/reissue"})
  public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return new ResponseEntity("No cookies found in the request", HttpStatus.BAD_REQUEST);
    }

    String refresh = null;
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals("refresh")) {
        refresh = cookie.getValue();
        break;
      }
    }

    if (refresh == null) {
      return new ResponseEntity("No refresh token found in the cookies", HttpStatus.BAD_REQUEST);
    }

    try {
      this.jwtUtil.isExpired(refresh);
    } catch (ExpiredJwtException e) {
      return new ResponseEntity("Refresh token expired", HttpStatus.BAD_REQUEST);
    }

    String category = this.jwtUtil.getCategory(refresh);
    if (!category.equals("refresh")) {
      return new ResponseEntity("Invalid refresh token", HttpStatus.BAD_REQUEST);
    }

    Boolean isExist = this.refreshRepository.existsByRefresh(refresh);
    if (!isExist) {
      return new ResponseEntity("Invalid refresh token", HttpStatus.BAD_REQUEST);
    }

    String username = this.jwtUtil.getUsername(refresh);
    String role = this.jwtUtil.getRole(refresh);
    String newAccess = this.jwtUtil.createJwt("access", username, role, 600000L);
    String newRefresh = this.jwtUtil.createJwt("refresh", username, role, 86400000L);
    this.refreshRepository.deleteByRefresh(refresh);
    addRefreshEntity(username, newRefresh, 86400000L);
    response.setHeader("access", newAccess);
    response.addCookie(createCookie("refresh", newRefresh));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void addRefreshEntity(String username, String refresh, Long expiredMs) {
    Date date = new Date(System.currentTimeMillis() + expiredMs);
    RefreshEntity refreshEntity = RefreshEntity.builder()
        .username(username)
        .refresh(refresh)
        .expiration(date.toString())
        .build();
    this.refreshRepository.save(refreshEntity);
  }

  private Cookie createCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(86400);
    cookie.setHttpOnly(true);
    return cookie;
  }
}
