package com.splitwise.controller;

import java.util.List;

import com.splitwise.controller.request.AddUserToActivityRequest;

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

  @PostMapping("/users")
  public ResponseEntity addUsersToActivity(@RequestBody AddUserToActivityRequest request) {
    activityService.addUserToActivity(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{activityId}")
  public ResponseEntity<Activity> getActivityById(@PathVariable long activityId) {
    return activityService.getActivityById(activityId)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<Activity>> getActivityByUserId(@RequestParam("userid") long userId) {
    return ResponseEntity.ok(activityService.getActivitiesByUserId(userId));
  }
}
