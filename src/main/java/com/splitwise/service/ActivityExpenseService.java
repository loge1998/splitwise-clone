package com.splitwise.service;

import java.util.Arrays;
import java.util.Optional;

import com.splitwise.controller.request.AddUserToActivityRequest;

import org.springframework.stereotype.Service;

import com.splitwise.controller.request.AddExpenseToActivityRequest;
import com.splitwise.exceptions.ResourceNotFoundException;
import com.splitwise.model.Expense;
import com.splitwise.repository.ExpenseRepository;

@Service
public class ActivityExpenseService {

  private final ActivityService activityService;
  private final UserService userService;
  private final ExpenseRepository expenseRepository;

  public ActivityExpenseService(
    ActivityService activityService, UserService userService, ExpenseRepository expenseRepository) {
    this.activityService = activityService;
    this.userService = userService;
    this.expenseRepository = expenseRepository;
  }

  public Expense addExpenseToActivity(AddExpenseToActivityRequest request) {
    activityService.addUserToActivity(new AddUserToActivityRequest(request.activityId(), request.userIds()));
    var user = userService.getUserByUserId(request.userWhoPaid())
      .orElseThrow(() -> new ResourceNotFoundException("Provided User not found: " + request.userWhoPaid()));
    var activity = activityService.getActivityById(request.activityId())
      .orElseThrow(() -> new ResourceNotFoundException("Provided Activity not found: " + request.activityId()));
    return expenseRepository.save(new Expense(request.description(), user, request.totalAmount(), activity));
  }
}
