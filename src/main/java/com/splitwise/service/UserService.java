package com.splitwise.service;

import java.util.Optional;

import com.splitwise.model.User;
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

  public Optional<User> getUserByUserId(long userId) {
    return userRepository.findById(userId);
  }

  public Optional<User> getUserByEmailId(String emailId) {
      return userRepository.findByEmailId(emailId);
  }
}
