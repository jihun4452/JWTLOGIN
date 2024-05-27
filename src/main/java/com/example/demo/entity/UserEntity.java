package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity

public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)

  private Integer id;
  private String username;
  private String password;
  private String role;
}
