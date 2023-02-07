package com.splitwise.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.splitwise.model.UserActivityMapping;
import com.splitwise.model.UserActivityMappingId;
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
  public void addActivity_shouldReturnNotFound_WhenProvidedUserInAddActivityRequestIsUnknown() throws Exception {
    AddActivityRequest request = new AddActivityRequest("testActivity", List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
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
  public void addUserToActivity_ShouldReturnBadRequest_WhenProvidedUserIsUnknown() throws Exception {
    Activity activity = addActivity(new Activity("testActivity"));
    AddUserToActivityRequest request = new AddUserToActivityRequest(activity.getId(), List.of(1L));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities/users")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$").value("Received request with unknown users: [1]"));
  }

  @Test
  public void getActivityById_ShouldReturnActivity_WhenActivityIdIsPresent() throws Exception {
    Activity activity = addActivity(new Activity("testActivity"));
    String activityId = String.valueOf(activity.getId());
    mvc.perform(MockMvcRequestBuilders.get("/activities/" + activityId)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value(activity.getName()))
      .andExpect(jsonPath("$.id").value(activity.getId()))
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  public void getActivityById_ShouldReturnNotFound_WhenActivityIdIsNotPresent() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/activities/10")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  public void getActivityByUserId_ShouldReturnNotFound_WhenUserIdIsNotPresent() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/activities")
        .queryParam("userid", "1")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  public void getActivityByUserId_ShouldReturnAllActivityThatUserBelongsTo() throws Exception {
    User userToAdd = addUser(new User("testUser", "testEmail"));
    Activity firstActivity = addActivity(new Activity("testActivity-1"));
    Activity secondActivity = addActivity(new Activity("testActivity-2"));
    addMapping(userToAdd.getId(), firstActivity.getId());
    addMapping(userToAdd.getId(), secondActivity.getId());

    mvc.perform(MockMvcRequestBuilders.get("/activities")
        .queryParam("userid", String.valueOf(userToAdd.getId()))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.[0].name").value(firstActivity.getName()))
      .andExpect(jsonPath("$.[0].id").value(firstActivity.getId()))
      .andExpect(jsonPath("$.[0].createdAt").exists())
      .andExpect(jsonPath("$.[0].updatedAt").exists())
      .andExpect(jsonPath("$.[1].name").value(secondActivity.getName()))
      .andExpect(jsonPath("$.[1].id").value(secondActivity.getId()))
      .andExpect(jsonPath("$.[1].createdAt").exists())
      .andExpect(jsonPath("$.[1].updatedAt").exists());
  }

  private UserActivityMapping addMapping(long userId, long activityId) {
    return userActivityMappingRepository.save(new UserActivityMapping(new UserActivityMappingId(userId, activityId)));
  }

  private Activity addActivity(Activity activity) {
    return activityRepository.save(activity);
  }

  private User addUser(User user) {
    return userRepository.save(user);
  }
}
