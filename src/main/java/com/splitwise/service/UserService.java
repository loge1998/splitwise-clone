package com.splitwise.service;

import com.splitwise.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public boolean checkIfUserPresent(Long userId) {
    return userRepository.findById(userId).isPresent();
  }
}
