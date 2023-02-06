package com.splitwise.service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.splitwise.controller.request.AddUserToActivityRequest;

import com.splitwise.exceptions.BadRequestException;
import com.splitwise.model.UserActivityMapping;
import com.splitwise.model.UserActivityMappingId;
import com.splitwise.repository.UserActivityMappingRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.splitwise.controller.request.AddActivityRequest;
import com.splitwise.model.Activity;
import com.splitwise.repository.ActivityRepository;

@Service
@Slf4j
public class ActivityService {
  private final ActivityRepository activityRepository;
  private final UserActivityMappingRepository userActivityMappingRepository;
  private final UserService userService;

  public ActivityService(
    ActivityRepository activityRepository,
    UserActivityMappingRepository userActivityMappingRepository,
    UserService userService) {
    this.activityRepository = activityRepository;
    this.userActivityMappingRepository = userActivityMappingRepository;
    this.userService = userService;
  }

  public Activity addActivity(AddActivityRequest request) {
    validateUserIds(request.userIds());
    Activity savedActivity = activityRepository.save(new Activity(request.name()));
    request.userIds()
      .stream()
      .map(userId -> new UserActivityMappingId(userId, savedActivity.getId()))
      .map(UserActivityMapping::new)
      .forEach(userActivityMappingRepository::save);
    return savedActivity;
  }

  public void addUserToActivity(AddUserToActivityRequest request) {
    validateUserIds(request.userIds());
    request.userIds()
      .stream()
      .map(userId -> new UserActivityMappingId(userId, request.activityId()))
      .map(UserActivityMapping::new)
      .forEach(userActivityMappingRepository::save);
  }

  private void validateUserIds(List<Long> userIds) {
    List<Long> unknownUserIds = userIds.stream()
      .filter(Predicate.not(userService::checkIfUserPresent))
      .collect(Collectors.toList());

    if(!unknownUserIds.isEmpty())
    {
      String message = "Received request with unknown users: " + unknownUserIds;
      log.error(message);
      throw new BadRequestException(message);
    }
  }
}
