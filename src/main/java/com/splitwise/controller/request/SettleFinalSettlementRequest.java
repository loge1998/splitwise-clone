package com.splitwise.controller.request;

public record SettleFinalSettlementRequest(Long paidUser, Long borrowedUser, Long activityId) {
}
