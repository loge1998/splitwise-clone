package com.splitwise.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinalSettlementId implements Serializable {
    @Column(name = "paid_user")
  private Long paidUser;
  @Column(name = "borrowed_user")
  private Long borrowedUser;
  @Column(name = "activity_id")
  private Long activityId;
}
