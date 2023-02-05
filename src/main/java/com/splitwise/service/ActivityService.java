package com.splitwise.service;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.splitwise.controller.request.AddActivityRequest;
import com.splitwise.model.Activity;
import com.splitwise.repository.ActivityRepository;

@Service
@Slf4j
public class ActivityService {
  private final ActivityRepository activityRepository;

  public ActivityService(ActivityRepository activityRepository) {
    this.activityRepository = activityRepository;
  }

  public Activity addActivity(AddActivityRequest request) {
    return activityRepository.save(new Activity(request.name(), LocalDateTime.now(), LocalDateTime.now()));
  }
}
