package com.splitwise.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.splitwise.model.UserActivityMappingId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.splitwise.controller.request.AddActivityRequest;
import com.splitwise.controller.request.AddUserToActivityRequest;
import com.splitwise.model.Activity;
import com.splitwise.model.User;
import com.splitwise.repository.ActivityRepository;
import com.splitwise.repository.UserActivityMappingRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.utils.JsonMessageParser;

@SpringBootTest
@AutoConfigureMockMvc
public class ActivityControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserActivityMappingRepository userActivityMappingRepository;

  @Autowired
  private JsonMessageParser parser;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    activityRepository.deleteAll();
    userActivityMappingRepository.deleteAll();
  }

  @Test
  public void shouldBeAbleToCreateActivity() throws Exception {
    User creatingUser = addUser(new User("testUser","testEmail"));
    AddActivityRequest request = new AddActivityRequest("testActivity", List.of(creatingUser.getId()));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("testActivity"))
      .andExpect(jsonPath("$.id").exists())
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  public void shouldReturnBadRequestWhenProvidedUserInAddActivityRequestIsUnknown() throws Exception {
    AddActivityRequest request = new AddActivityRequest("testActivity", List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$").value("Received request with unknown users: [1]"));
  }

  @Test
  public void shouldBeAbleToAddExistingUserToActivity() throws Exception {
    User userToAdd = addUser(new User("testUser", "testEmail"));
    Activity activity = addActivity(new Activity("testActivity"));
    AddUserToActivityRequest request = new AddUserToActivityRequest(activity.getId(), List.of(userToAdd.getId()));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/users")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    assertTrue(userActivityMappingRepository.findById(new UserActivityMappingId(userToAdd.getId(), activity.getId()))
      .isPresent());
  }

  @Test
  public void shouldReturnBadRequestWhenProvidedUserIsUnknown() throws Exception {
    Activity activity = addActivity(new Activity("testActivity"));
    AddUserToActivityRequest request = new AddUserToActivityRequest(activity.getId(), List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/users")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$").value("Received request with unknown users: [1]"));
  }

  private Activity addActivity(Activity activity) {
    return activityRepository.save(activity);
  }

  private User addUser(User user) {
    return userRepository.save(user);
  }
}
