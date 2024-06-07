package com.example.demo.dto;

import com.example.demo.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class JoinDTO {
  private String username;
  private String userEmail;
  private String password;

  public UserEntity toEntity() {
    return UserEntity.builder()
        .username(this.username)
        .useremail(this.userEmail)
        .password(this.password)
        .build();
  }
}
