package com.splitwise.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.vavr.Tuple2;
import org.springframework.stereotype.Service;

import com.splitwise.controller.request.AddExpenseToActivityRequest;
import com.splitwise.controller.request.AddUserToActivityRequest;
import com.splitwise.controller.request.SettleFinalSettlementRequest;
import com.splitwise.exceptions.ResourceNotFoundException;
import com.splitwise.model.Expense;
import com.splitwise.model.FinalSettlement;
import com.splitwise.model.FinalSettlementId;
import com.splitwise.model.User;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.FinalSettlementRepository;

@Service
public class ActivityExpenseService {

  private final ActivityService activityService;
  private final UserService userService;
  private final ExpenseRepository expenseRepository;
  private final FinalSettlementRepository finalSettlementRepository;

  public ActivityExpenseService(
    ActivityService activityService,
    UserService userService,
    ExpenseRepository expenseRepository,
    FinalSettlementRepository finalSettlementRepository) {
    this.activityService = activityService;
    this.userService = userService;
    this.expenseRepository = expenseRepository;
    this.finalSettlementRepository = finalSettlementRepository;
  }

  public Expense addExpenseToActivity(AddExpenseToActivityRequest request) {
    activityService.validateActivityId(request.activityId());
    activityService.addUserToActivity(new AddUserToActivityRequest(request.activityId(), request.userIds()));
    var user = userService.getUserByUserId(request.userWhoPaid())
      .orElseThrow(() -> new ResourceNotFoundException("Provided User not found: " + request.userWhoPaid()));
    var activity = activityService.getActivityById(request.activityId())
      .orElseThrow(() -> new ResourceNotFoundException("Provided Activity not found: " + request.activityId()));
    var borrowedUsers = request.userIds()
      .stream()
      .map(userService::getUserByUserId)
      .flatMap(Optional::stream)
      .collect(Collectors.toSet());
    return expenseRepository.save(new Expense(
      request.description(),
      user,
      request.totalAmount(),
      activity,
      borrowedUsers));
  }

  public List<Expense> getExpensesForActivity(long activityId) {
    activityService.validateActivityId(activityId);
    return expenseRepository.findAllByActivityId(activityId);
  }

  public List<FinalSettlement> prepareSettlements(long activityId) {
    List<FinalSettlement> settlements = getExpensesForActivity(activityId)
      .stream()
      .map(this::convertToFinalSettlements)
      .flatMap(List::stream)
      .collect(Collectors.toList());

    return simplifySettlements(settlements)
      .stream()
      .map(finalSettlementRepository::save)
      .collect(Collectors.toList());
  }

  public FinalSettlement settleFinalSettlement(SettleFinalSettlementRequest request) {
    return finalSettlementRepository.findById(new FinalSettlementId(request.paidUser(), request.borrowedUser(),
        request.activityId()))
      .map(FinalSettlement::settle)
      .map(finalSettlementRepository::save)
      .orElseThrow(() -> new ResourceNotFoundException("Settlement not Found: " + request));
  }

  public List<FinalSettlement> getSettlements(long activityId) {
    activityService.validateActivityId(activityId);
    return finalSettlementRepository.findByIdActivityId(activityId);
  }

  private List<FinalSettlement> simplifySettlements(List<FinalSettlement> finalSettlements) {
    HashMap<Tuple2<User,User>,FinalSettlement> settlementHashMap = new HashMap<>();
    finalSettlements.forEach(currentSettlement -> updateMapping(settlementHashMap, currentSettlement));

    return settlementHashMap.values()
      .stream()
      .filter(finalSettlement -> finalSettlement.getSettlementAmount().compareTo(BigDecimal.ZERO) != 0)
      .map(finalSettlement -> {
        if(finalSettlement.getSettlementAmount().compareTo(BigDecimal.ZERO) < 0) {
          return finalSettlement.inverseSettlement();
        } else
          return finalSettlement;
      })
      .collect(Collectors.toList());
  }

  private void updateMapping(HashMap<Tuple2<User,User>,FinalSettlement> settlementHashMap, FinalSettlement currentSettlement) {
    var userPair = new Tuple2<>(currentSettlement.getPaidUser(), currentSettlement.getBorrowedUser());
    var inversePair = new Tuple2<>(currentSettlement.getBorrowedUser(), currentSettlement.getPaidUser());
    if(settlementHashMap.containsKey(userPair)) {
      var existingSettlement = settlementHashMap.get(userPair);
      existingSettlement.addAmount(currentSettlement.getSettlementAmount());
      settlementHashMap.put(userPair, existingSettlement);
    } else if(settlementHashMap.containsKey(inversePair)) {
      var existingSettlement = settlementHashMap.get(inversePair);
        existingSettlement.subtractAmount(currentSettlement.getSettlementAmount());
      settlementHashMap.put(inversePair, existingSettlement);
    } else {
      settlementHashMap.put(userPair, currentSettlement);
    }
  }

  private List<FinalSettlement> convertToFinalSettlements(Expense expense) {
    int totalShares = expense.getBorrowedUsers().size() + 1;
    BigDecimal amountToSettle = expense.getTotalAmount().divide(BigDecimal.valueOf(totalShares),2, RoundingMode.CEILING);
    return expense.getBorrowedUsers()
      .stream()
      .map(borrowedUser -> new FinalSettlement(
        expense.getUserWhoPaid(),
        borrowedUser,
        expense.getActivity(),
        amountToSettle))
      .collect(Collectors.toList());
  }
}
