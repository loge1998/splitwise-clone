package com.splitwise.controller.response;

import java.math.BigDecimal;

public record FinalSettlementResponse(Long paidUser, Long borrowedUser, Long activityId, BigDecimal settlementAmount,
                                      Boolean isSettled) {
}
