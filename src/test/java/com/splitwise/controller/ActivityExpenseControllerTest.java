package com.splitwise.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.splitwise.controller.request.AddExpenseToActivityRequest;
import com.splitwise.model.Activity;
import com.splitwise.model.User;
import com.splitwise.repository.ActivityRepository;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.utils.JsonMessageParser;

@SpringBootTest
@AutoConfigureMockMvc
class ActivityExpenseControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JsonMessageParser parser;

  @BeforeEach
  void setUp() {
    expenseRepository.deleteAll();
    activityRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void addExpenseToActivity_ShouldReturnNotFound_WhenActivityNotFound() throws Exception {
    AddExpenseToActivityRequest request = new AddExpenseToActivityRequest(1L, "testDescription", BigDecimal.TEN, 2L, List.of(1L));
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
    addActivity(new Activity("testActivity"));
    AddExpenseToActivityRequest request = new AddExpenseToActivityRequest(1L, "testDescription", BigDecimal.TEN, 2L, List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/1/expenses")
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
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());

    assertTrue(userRepository.findById(firstUser.getId()).isPresent());
    assertTrue(userRepository.findById(secondUser.getId()).isPresent());
    assertTrue(activityRepository.findById(activity.getId()).isPresent());
    assertEquals(1, expenseRepository.findAll().size());
  }

  private Activity addActivity(Activity activity) {
    return activityRepository.save(activity);
  }

  private User addUser(User user) {
    return userRepository.save(user);
  }
}