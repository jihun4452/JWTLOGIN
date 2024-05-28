package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "refresh_entity")
public class RefreshEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;
  private String refresh;
  private String expiration;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;
}