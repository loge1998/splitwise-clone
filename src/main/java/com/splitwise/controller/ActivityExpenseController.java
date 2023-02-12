package com.splitwise.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.splitwise.controller.request.AddExpenseToActivityRequest;
import com.splitwise.controller.request.SettleFinalSettlementRequest;
import com.splitwise.controller.response.FinalSettlementResponse;
import com.splitwise.model.Expense;
import com.splitwise.model.FinalSettlement;
import com.splitwise.service.ActivityExpenseService;

@RestController
@RequestMapping("/activities/{activityId}")
public class ActivityExpenseController {

  private final ActivityExpenseService activityExpenseService;

  public ActivityExpenseController(ActivityExpenseService activityExpenseService) {
    this.activityExpenseService = activityExpenseService;
  }

  @PostMapping("/expenses")
  public ResponseEntity<Expense> addExpense(@RequestBody AddExpenseToActivityRequest request) {
    return ResponseEntity.ok(activityExpenseService.addExpenseToActivity(request));
  }

  @GetMapping("/expenses")
  public ResponseEntity<List<Expense>> getExpenses(@PathVariable("activityId") Long activityId) {
    return ResponseEntity.ok(activityExpenseService.getExpensesForActivity(activityId));
  }

  @PostMapping("/prepare-settlements")
  public ResponseEntity<List<FinalSettlementResponse>> prepareSettlement(@PathVariable("activityId") long activityId) {
    return ResponseEntity.ok(activityExpenseService.prepareSettlements(activityId)
      .stream()
      .map(FinalSettlement::toResponse)
      .collect(Collectors.toList()));
  }

  @GetMapping("/settlements")
  public ResponseEntity<List<FinalSettlementResponse>> getSettlements(@PathVariable("activityId") long activityId) {
    return ResponseEntity.ok(activityExpenseService.getSettlements(activityId)
      .stream()
      .map(FinalSettlement::toResponse)
      .collect(Collectors.toList()));
  }

  @PostMapping("/settlements/settle")
  public ResponseEntity<FinalSettlementResponse> settle(SettleFinalSettlementRequest request) {
    return ResponseEntity.ok(activityExpenseService.settleFinalSettlement(request).toResponse());
  }
}
