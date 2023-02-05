package com.splitwise.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.splitwise.controller.request.AddActivityRequest;
import com.splitwise.model.Activity;
import com.splitwise.service.ActivityService;

@RestController
@RequestMapping("/activities")
public class ActivityController {
  
  private final ActivityService activityService;

  public ActivityController(ActivityService activityService) {
    this.activityService = activityService;
  }

  @PostMapping
  public ResponseEntity<Activity> createActivity(@RequestBody AddActivityRequest request) {
    return ResponseEntity.ok(activityService.addActivity(request));
  }
}
