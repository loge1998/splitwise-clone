package com.splitwise.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.splitwise.controller.request.AddActivityRequest;
import com.splitwise.controller.request.AddUserToActivityRequest;
import com.splitwise.exceptions.ResourceNotFoundException;
import com.splitwise.model.Activity;
import com.splitwise.model.User;
import com.splitwise.repository.ActivityRepository;

@Service
@Slf4j
public class ActivityService {
  private final ActivityRepository activityRepository;
  private final UserService userService;

  public ActivityService(
    ActivityRepository activityRepository,
    UserService userService) {
    this.activityRepository = activityRepository;
    this.userService = userService;
  }

  public Activity addActivity(AddActivityRequest request) {
    userService.validateUserIds(request.userIds());
    List<User> users = request.userIds()
      .stream()
      .map(userService::getUserByUserId)
      .flatMap(Optional::stream)
      .collect(Collectors.toList());
    return activityRepository.save(new Activity(request.name(), users));
  }

  public void addUserToActivity(AddUserToActivityRequest request) {
    validateActivityId(request.activityId());
    userService.validateUserIds(request.userIds());
    Set<User> users = request.userIds()
      .stream()
      .map(userService::getUserByUserId)
      .flatMap(Optional::stream)
      .collect(Collectors.toSet());

    getActivityById(request.activityId())
      .ifPresent(activity -> {
        activity.setUsers(users);
        activityRepository.save(activity);
      });
  }

  public void validateActivityId(Long activityId) {
    activityRepository.findById(activityId)
      .orElseThrow(() -> new ResourceNotFoundException("Received request with unknown activity: " + activityId));
  }

  public Optional<Activity> getActivityById(long activityId) {
    return activityRepository.findById(activityId);
  }

  public List<Activity> getActivitiesByUserId(long userId) {
    return userService.getUserByUserId(userId)
      .map(User::getActivities)
      .map(ArrayList::new)
      .orElseThrow(() -> new ResourceNotFoundException("Received request with unknown user: " + userId));
  }
}
