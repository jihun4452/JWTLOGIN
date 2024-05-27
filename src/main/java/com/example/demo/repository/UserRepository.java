package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <UserEntity, Integer>{
//유저가 존재하는지 확인
  Boolean existsByUsername(String username);

  UserEntity findByUsername(String username);
}