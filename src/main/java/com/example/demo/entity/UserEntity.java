package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "user_entity")
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;
  private String useremail;

  private String password;

  private String role;

  public UserEntity(String username, String useremail, String password, String role) {
    this.username = username;
    this.useremail = useremail;
    this.password = password;
    this.role = role;
  }
}
