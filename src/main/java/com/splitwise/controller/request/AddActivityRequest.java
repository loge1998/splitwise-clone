package com.splitwise.controller.request;

import java.util.List;

public record AddActivityRequest (String name, List<Long> userIds) {}

