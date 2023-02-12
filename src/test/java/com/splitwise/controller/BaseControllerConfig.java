package com.splitwise.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.splitwise.repository.*;
import com.splitwise.utils.JsonMessageParser;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerConfig {

  @Autowired
  protected MockMvc mvc;

  @Autowired
  protected ActivityRepository activityRepository;

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected UserActivityMappingRepository userActivityMappingRepository;

  @Autowired
  protected JsonMessageParser parser;

  @Autowired
  protected ExpenseRepository expenseRepository;

  @Autowired
  protected FinalSettlementRepository finalSettlementRepository;

  @BeforeEach
  void setUp() {
    userActivityMappingRepository.deleteAll();
    finalSettlementRepository.deleteAll();
    expenseRepository.deleteAll();
    activityRepository.deleteAll();
    userRepository.deleteAll();
  }
}
