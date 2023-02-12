package com.splitwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.splitwise.model.Activity;
import com.splitwise.model.Expense;
import com.splitwise.model.FinalSettlement;
import com.splitwise.model.User;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.FinalSettlementRepository;

@SpringBootTest
class ActivityExpenseServiceTest {

  @MockBean
  private ActivityService activityService;

  @MockBean
  private ExpenseRepository expenseRepository;

  @MockBean
  private FinalSettlementRepository finalSettlementRepository;

  @Autowired
  private ActivityExpenseService activityExpenseService;

  private final User user1 = new User("user1", "email1");
  private final User user2 = new User("user2", "email2");
  private final User user3 = new User("user3", "email3");
  private final User user4 = new User("user4", "email4");
  private final Activity activity = new Activity("test");

  @Test
  void prepareSettlements_ShouldReturnAllSettlementsAsItIs_whenThereIsNoSimplificationPossible() {
    long activityId = 1L;

    List<Expense> expenses = List.of(new Expense("desc1", user1, BigDecimal.TEN, activity, Set.of(user2, user3, user4)));

    Mockito.when(expenseRepository.findAllByActivityId(activityId)).thenReturn(expenses);
    Mockito.when(finalSettlementRepository.save(Mockito.any(FinalSettlement.class)))
      .then(returnsFirstArg());

    List<FinalSettlement> finalSettlements = activityExpenseService.prepareSettlements(activityId);

    assertEquals(3, finalSettlements.size());
    assertEquals("2.50", finalSettlements.get(0).getSettlementAmount().toString());
    assertEquals("2.50", finalSettlements.get(1).getSettlementAmount().toString());
    assertEquals("2.50", finalSettlements.get(2).getSettlementAmount().toString());

    assertEquals(finalSettlements.get(0).getPaidUser(), user1);
    assertEquals(finalSettlements.get(1).getPaidUser(), user1);
    assertEquals(finalSettlements.get(2).getPaidUser(), user1);
  }

  @Test
  void prepareSettlements_shouldIgnoreSettlementsWithZeroAmount_AfterSimplification() {
    long activityId = 1L;

    List<Expense> expenses = List.of(new Expense("desc1", user1, BigDecimal.TEN, activity, Set.of(user2, user3, user4)),
      new Expense("desc2", user2, BigDecimal.TEN, activity, Set.of(user1, user3, user4)));

    Mockito.when(expenseRepository.findAllByActivityId(activityId)).thenReturn(expenses);
    Mockito.when(finalSettlementRepository.save(Mockito.any(FinalSettlement.class)))
      .then(returnsFirstArg());

    List<FinalSettlement> finalSettlements = activityExpenseService.prepareSettlements(activityId);
    System.out.println(finalSettlements);
    assertEquals(4, finalSettlements.size());
    assertEquals("2.50", finalSettlements.get(0).getSettlementAmount().toString());
    assertEquals("2.50", finalSettlements.get(1).getSettlementAmount().toString());
    assertEquals("2.50", finalSettlements.get(2).getSettlementAmount().toString());
    assertEquals("2.50", finalSettlements.get(3).getSettlementAmount().toString());

    List<Tuple2<User,User>> userTuples = finalSettlements.stream()
      .map(finalSettlement -> new Tuple2<>(finalSettlement.getPaidUser(), finalSettlement.getBorrowedUser()))
      .collect(Collectors.toList());

    assertTrue(userTuples.contains(new Tuple2<>(user1, user3)));
    assertTrue(userTuples.contains(new Tuple2<>(user1, user4)));
    assertTrue(userTuples.contains(new Tuple2<>(user2, user3)));
    assertTrue(userTuples.contains(new Tuple2<>(user2, user4)));
  }

  @Test
  void prepareSettlements_shouldCombineSettlementsBetweenUsers_AfterSimplification() {
    long activityId = 1L;

    List<Expense> expenses = List.of(new Expense("desc1", user1, BigDecimal.valueOf(1500), activity, Set.of(user2, user3, user4)),
      new Expense("desc2", user2, BigDecimal.valueOf(500), activity, Set.of(user1, user3, user4)),
      new Expense("desc3", user1, BigDecimal.valueOf(500), activity, Set.of(user2, user3, user4)),
      new Expense("desc3", user4, BigDecimal.valueOf(250), activity, Set.of(user1, user2, user3)),
      new Expense("desc3", user3, BigDecimal.valueOf(2500), activity, Set.of(user1, user2, user4))
      );

    Mockito.when(expenseRepository.findAllByActivityId(activityId)).thenReturn(expenses);
    Mockito.when(finalSettlementRepository.save(Mockito.any(FinalSettlement.class)))
      .then(returnsFirstArg());

    List<FinalSettlement> finalSettlements = activityExpenseService.prepareSettlements(activityId);
    assertEquals(6, finalSettlements.size());

    List<Tuple3<User, User, String>> userTuples = finalSettlements.stream()
      .map(finalSettlement -> new Tuple3<>(finalSettlement.getPaidUser(), finalSettlement.getBorrowedUser(), finalSettlement.getSettlementAmount().toString()))
      .collect(Collectors.toList());

    System.out.println(userTuples);
    assertTrue(userTuples.contains(new Tuple3<>(user1, user4, "437.50")));
    assertTrue(userTuples.contains(new Tuple3<>(user1, user2, "375.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user2, user4, "62.50")));
    assertTrue(userTuples.contains(new Tuple3<>(user3, user1, "125.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user3, user2, "500.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user3, user4, "562.50")));
  }
}