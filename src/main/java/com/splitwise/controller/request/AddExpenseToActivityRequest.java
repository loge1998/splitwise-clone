package com.splitwise.controller.request;

import java.math.BigDecimal;
import java.util.List;

public record AddExpenseToActivityRequest(long activityId, String description, BigDecimal totalAmount, Long userWhoPaid, List<Long> userIds) {
}
