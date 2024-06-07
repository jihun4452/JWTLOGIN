package com.example.demo.jwt;

import com.example.demo.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class CustomLogoutFilter extends GenericFilterBean {

  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;

  public CustomLogoutFilter(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
    this.jwtUtil = jwtUtil;
    this.refreshRepository = refreshRepository;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    // 로그아웃 요청인지 확인
    if (!isLogoutRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 쿠키에서 Refresh 토큰 가져오기
    String refresh = getRefreshTokenFromCookie(request);

    if (refresh == null) {
      // Refresh 토큰이 없는 경우
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    try {
      // Refresh 토큰 검증
      jwtUtil.isExpired(refresh);
    } catch (ExpiredJwtException e) {
      // Refresh 토큰이 만료된 경우
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // Refresh 토큰이 refresh 카테고리인지 확인
    String category = jwtUtil.getCategory(refresh);
    if (!category.equals("refresh")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // DB에 해당 Refresh 토큰이 존재하는지 확인
    Boolean isExist = refreshRepository.existsByRefresh(refresh);
    if (!isExist) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // 로그아웃 처리: Refresh 토큰 DB에서 제거
    refreshRepository.deleteByRefresh(refresh);

    // 쿠키에서 Refresh 토큰 삭제
    removeRefreshTokenCookie(response);

    // 로그아웃 성공
    response.setStatus(HttpServletResponse.SC_OK);
  }

  private boolean isLogoutRequest(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    String requestMethod = request.getMethod();
    return requestUri.equals("/logout") && requestMethod.equals("POST");
  }

  private String getRefreshTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("refresh")) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  private void removeRefreshTokenCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie("refresh", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
  }
}
