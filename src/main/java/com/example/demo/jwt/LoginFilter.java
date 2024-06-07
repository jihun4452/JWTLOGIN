package com.example.demo.jwt;

import com.example.demo.entity.RefreshEntity;
import com.example.demo.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;
  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;

  public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository) {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.refreshRepository = refreshRepository;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    String username = obtainUsername(request);
    String password = obtainPassword(request);

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

    return authenticationManager.authenticate(authToken);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
    String username = authentication.getName();

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
    GrantedAuthority auth = iterator.next();
    String role = auth.getAuthority();

    // 토큰 생성
    String access = jwtUtil.createJwt("access", username, role, 600000L);
    String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

    // 응답 설정
    response.setHeader("access", access);
    response.setHeader("refresh", refresh);
    response.setStatus(HttpStatus.OK.value());

    // RefreshEntity 추가
    Date expiration = new Date(System.currentTimeMillis() + 86400000L);
    RefreshEntity refreshEntity = RefreshEntity.builder()
        .username(username)
        .refresh(refresh)
        .expiration(expiration.toString())
        .build();
    refreshRepository.save(refreshEntity);
  }

  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
  }
}
