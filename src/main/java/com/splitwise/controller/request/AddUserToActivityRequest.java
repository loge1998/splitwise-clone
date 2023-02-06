package com.splitwise.controller.request;

import java.util.List;

public record AddUserToActivityRequest(long activityId, List<Long> userIds) {
}
