package com.splitwise.repository;

import java.util.Optional;

import com.splitwise.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmailId(String emailId);
}
