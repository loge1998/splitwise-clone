package com.splitwise.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.Tuple3;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.splitwise.controller.request.AddExpenseToActivityRequest;
import com.splitwise.controller.response.FinalSettlementResponse;
import com.splitwise.model.Activity;
import com.splitwise.model.Expense;
import com.splitwise.model.User;

class ActivityExpenseControllerTest extends BaseControllerConfig {

  @Test
  void addExpenseToActivity_ShouldReturnNotFound_WhenActivityNotFound() throws Exception {
    AddExpenseToActivityRequest request = new AddExpenseToActivityRequest(
      1L,
      "testDescription",
      BigDecimal.TEN,
      2L,
      List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/1/expenses")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$").value("Received request with unknown activity: 1"));
  }

  @Test
  void addExpenseToActivity_ShouldReturnNotFound_WhenUserIdNotFound() throws Exception {
    Activity addedActivity = addActivity(new Activity("testActivity"));
    AddExpenseToActivityRequest request = new AddExpenseToActivityRequest(
      addedActivity.getId(),
      "testDescription",
      BigDecimal.TEN,
      2L,
      List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/" + addedActivity.getId() + "/expenses")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$").value("Received request with unknown users: [1]"));
  }

  @Test
  void addExpenseToActivity_ShouldReturnSuccess_WhenUsersAndActivityPresent() throws Exception {
    Activity activity = addActivity(new Activity("testActivity"));
    User firstUser = addUser(new User("testUser-1", "testEmail-1"));
    User secondUser = addUser(new User("testUser-2", "testEmail-2"));

    AddExpenseToActivityRequest request = new AddExpenseToActivityRequest(
      activity.getId(),
      "testDescription",
      BigDecimal.TEN,
      firstUser.getId(),
      List.of(secondUser.getId()));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/" + activity.getId() + "/expenses")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.description").value("testDescription"))
      .andExpect(jsonPath("$.id").exists());

    assertTrue(userRepository.findById(firstUser.getId()).isPresent());
    assertTrue(userRepository.findById(secondUser.getId()).isPresent());
    assertTrue(activityRepository.findById(activity.getId()).isPresent());
    assertEquals(1, expenseRepository.findAll().size());
  }

  @Test
  public void getExpensesForActivity_ShouldReturnNotFound_IfActivityNotPresent() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/activities/1/expenses")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  public void getExpensesForActivity_ShouldReturnExpenses() throws Exception {
    User user = userRepository.save(new User("testName", "testEmail"));
    Activity activity = activityRepository.save(new Activity("testActivity"));
    expenseRepository.save(new Expense("testDescription-1", user, BigDecimal.TEN, activity, Set.of()));
    expenseRepository.save(new Expense("testDescription-2", user, BigDecimal.ONE, activity, Set.of()));
    mvc.perform(MockMvcRequestBuilders.get("/activities/" + activity.getId() + "/expenses")
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$").isArray())
      .andExpect(jsonPath("$.[0].description").value("testDescription-1"))
      .andExpect(jsonPath("$.[0].totalAmount").value("10.0"))
      .andExpect(jsonPath("$.[0].userWhoPaid.name").value("testName"))
      .andExpect(jsonPath("$.[0].activity.name").value("testActivity"))
      .andExpect(jsonPath("$.[1].description").value("testDescription-2"))
      .andExpect(jsonPath("$.[1].totalAmount").value("1.0"))
      .andExpect(jsonPath("$.[1].userWhoPaid.name").value("testName"))
      .andExpect(jsonPath("$.[1].activity.name").value("testActivity"));
  }

  @Test
  public void prepareSettlements_shouldReturnNotFound_WhenActivityIdIsUnknown() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/activities/1/prepare-settlements")
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isNotFound());
  }

  @Test
  public void prepareSettlements_ShouldReturnFinalSettlements() throws Exception {
    Activity activity = addActivity(new Activity("testActivity"));
    User user1 = addUser(new User("user1", "email1"));
    User user2 = addUser(new User("user2", "email2"));
    User user3 = addUser(new User("user3", "email3"));
    User user4 = addUser(new User("user4", "email4"));

    Expense expense1 = expenseRepository.save(new Expense(
      "testDesc1",
      user1,
      BigDecimal.valueOf(100),
      activity,
      Set.of(user2, user3, user4)));
    Expense expense2 = expenseRepository.save(new Expense(
      "testDesc2",
      user2,
      BigDecimal.valueOf(100),
      activity,
      Set.of(user1, user3, user4)));
    Expense expense3 = expenseRepository.save(new Expense(
      "testDesc3",
      user3,
      BigDecimal.valueOf(1000),
      activity,
      Set.of(user1, user2, user4)));
    Expense expense4 = expenseRepository.save(new Expense(
      "testDesc4",
      user4,
      BigDecimal.valueOf(500),
      activity,
      Set.of(user1, user2, user3)));
    Expense expense5 = expenseRepository.save(new Expense(
      "testDesc5",
      user2,
      BigDecimal.valueOf(1500),
      activity,
      Set.of(user1, user3, user4)));

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(
          "/activities/" + activity.getId() + "/prepare-settlements")
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    List<FinalSettlementResponse> response = parser.readValue(
      mvcResult.getResponse().getContentAsString(),
      new TypeReference<List<FinalSettlementResponse>>() {
      }).get();

    List<Tuple3<Long,Long,String>> userTuples = response.stream()
      .map(finalSettlement -> new Tuple3<>(
        finalSettlement.paidUser(),
        finalSettlement.borrowedUser(),
        finalSettlement.settlementAmount().toString()))
      .collect(Collectors.toList());

    assertTrue(userTuples.contains(new Tuple3<>(user3.getId(), user4.getId(), "125.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user3.getId(), user1.getId(), "225.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user2.getId(), user4.getId(), "275.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user2.getId(), user1.getId(), "375.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user2.getId(), user3.getId(), "150.00")));
    assertTrue(userTuples.contains(new Tuple3<>(user4.getId(), user1.getId(), "100.00")));
  }

  private Activity addActivity(Activity activity) {
    return activityRepository.save(activity);
  }

  private User addUser(User user) {
    return userRepository.save(user);
  }
}