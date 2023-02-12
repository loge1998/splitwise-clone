package com.splitwise.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.splitwise.exceptions.ResourceNotFoundException;
import com.splitwise.model.User;
import com.splitwise.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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

  public void validateUserIds(List<Long> userIds) {
    List<Long> unknownUserIds = userIds.stream()
      .filter(Predicate.not(this::checkIfUserPresent))
      .collect(Collectors.toList());

    if(!unknownUserIds.isEmpty())
    {
      String message = "Received request with unknown users: " + unknownUserIds;
      log.error(message);
      throw new ResourceNotFoundException(message);
    }
  }

  public Optional<User> getUserByEmailId(String emailId) {
      return userRepository.findByEmailId(emailId);
  }
}
