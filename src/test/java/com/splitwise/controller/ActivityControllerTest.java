package com.splitwise.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import com.splitwise.controller.response.FinalSettlementResponse;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.splitwise.controller.request.AddActivityRequest;
import com.splitwise.controller.request.AddUserToActivityRequest;
import com.splitwise.model.Activity;
import com.splitwise.model.User;

public class ActivityControllerTest extends BaseControllerConfig {

  @Test
  public void shouldBeAbleToCreateActivity() throws Exception {
    User creatingUser = addUser(new User("testUser", "testEmail"));
    AddActivityRequest request = new AddActivityRequest("testActivity", List.of(creatingUser.getId()));
    String message = parser.toJson(request).get();
    mvc.perform(MockMvcRequestBuilders.post("/activities")
        .content(message)
        .header("Content-Type", "application/json")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("testActivity"))
      .andExpect(jsonPath("$.id").exists());
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
      .andExpect(jsonPath("$.id").value(activity.getId()));
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
    Activity firstActivity = addActivity(new Activity("testActivity-1", List.of(userToAdd)));
    Activity secondActivity = addActivity(new Activity("testActivity-2", List.of(userToAdd)));

    MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/activities")
        .queryParam("userid", String.valueOf(userToAdd.getId()))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andReturn();

    List<Activity> response = parser.readValue(
      mvcResult.getResponse().getContentAsString(),
      new TypeReference<List<Activity>>() {
      }).get();

    assertTrue(response.contains(firstActivity));
    assertTrue(response.contains(secondActivity));
  }

  private Activity addActivity(Activity activity) {
    return activityRepository.save(activity);
  }

  private User addUser(User user) {
    return userRepository.save(user);
  }
}
