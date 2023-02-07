package com.splitwise.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.splitwise.model.User;
import com.splitwise.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  public void getUserById_ShouldReturnNotFound_WhenUserIdIsNotPresent() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/users/10")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  public void getUserByEmailId_ShouldReturnNotFound_NoUserPresentWithGivenEmailId() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/users")
        .queryParam("emailid", "1")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isNotFound());
  }

  @Test
  public void getUserByEmailId_ShouldReturnUserWithGivenEmailId() throws Exception {
    User userToAdd = userRepository.save(new User("testUser", "testEmail"));

    mvc.perform(MockMvcRequestBuilders.get("/users")
        .queryParam("emailid", userToAdd.getEmailId())
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value(userToAdd.getName()))
      .andExpect(jsonPath("$.id").value(userToAdd.getId()))
      .andExpect(jsonPath("$.emailId").value(userToAdd.getEmailId()))
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  public void getUserById_ShouldReturnUserWithId() throws Exception {
    User userToAdd = userRepository.save(new User("testUser", "testEmail"));

    mvc.perform(MockMvcRequestBuilders.get("/users/" + userToAdd.getId())
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value(userToAdd.getName()))
      .andExpect(jsonPath("$.id").value(userToAdd.getId()))
      .andExpect(jsonPath("$.emailId").value(userToAdd.getEmailId()))
      .andExpect(jsonPath("$.createdAt").exists())
      .andExpect(jsonPath("$.updatedAt").exists());
  }
}
