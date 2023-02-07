package com.splitwise.controller;

import com.splitwise.controller.request.AddExpenseToActivityRequest;

import com.splitwise.model.Expense;

import com.splitwise.service.ActivityExpenseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activities/{activityId}/expenses")
public class ActivityExpenseController {

  private final ActivityExpenseService activityExpenseService;

  public ActivityExpenseController(ActivityExpenseService activityExpenseService) {
    this.activityExpenseService = activityExpenseService;
  }

  @PostMapping
  public ResponseEntity<Expense> addExpense(@RequestBody AddExpenseToActivityRequest request) {
    return ResponseEntity.ok(activityExpenseService.addExpenseToActivity(request));
  }
}
