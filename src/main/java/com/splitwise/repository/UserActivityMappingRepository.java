package com.splitwise.repository;

import java.util.List;

import com.splitwise.model.UserActivityMapping;
import com.splitwise.model.UserActivityMappingId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityMappingRepository extends JpaRepository<UserActivityMapping,UserActivityMappingId> {
  List<UserActivityMapping> findByMappingIdUserId(long userId);
}
