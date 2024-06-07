package com.example.demo.Service;

import com.example.demo.dto.JoinDTO;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class JoinService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Transactional
  public void joinProcess(JoinDTO joinDTO) {
    String username = joinDTO.getUsername();
    String userEmail = joinDTO.getUserEmail();
    String password = joinDTO.getPassword();

    if (userRepository.existsByUsername(username)) {
      return;
    }

    UserEntity data = UserEntity.builder()
        .username(username)
        .useremail(userEmail)
        .password(bCryptPasswordEncoder.encode(password))
        .role("ROLE_ADMIN")
        .build();

    userRepository.save(data);
  }
}
