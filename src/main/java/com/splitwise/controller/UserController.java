package com.splitwise.controller;

import com.splitwise.model.User;
import com.splitwise.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{userId}")
  public ResponseEntity<User> getUserById(@PathVariable long userId) {
    return userService.getUserByUserId(userId)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<User> getUserByEmailId(@RequestParam("emailid") String emailId) {
    return userService.getUserByEmailId(emailId)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
