package com.splitwise.model;

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.splitwise.controller.response.FinalSettlementResponse;

@Entity
@Table(name = "final_settlement")
@AllArgsConstructor
@Data
@NoArgsConstructor
public class FinalSettlement {

  @EmbeddedId
  private FinalSettlementId id;

  @ManyToOne
  @MapsId("paidUser")
  @JoinColumn(name = "paid_user")
  private User paidUser;

  @ManyToOne
  @MapsId("borrowedUser")
  @JoinColumn(name = "borrowed_user")
  private User borrowedUser;

  @ManyToOne
  @MapsId("activityId")
  @JoinColumn(name = "activityId")
  private Activity activity;

  @Column(name = "settlement_amount")
  private BigDecimal settlementAmount;

  @Column(name = "is_settled")
  private Boolean isSettled;

  public FinalSettlement(User paidUser,
    User borrowedUser,
    Activity activity,
    BigDecimal settlementAmount) {
    this.id = new FinalSettlementId(paidUser.getId(), borrowedUser.getId(), activity.getId());
    this.paidUser = paidUser;
    this.borrowedUser = borrowedUser;
    this.activity = activity;
    this.settlementAmount = settlementAmount;
    this.isSettled = false;
  }

  public FinalSettlement inverseSettlement() {
    return new FinalSettlement(
      borrowedUser,
      paidUser,
      activity,
      settlementAmount.negate());
  }

  public FinalSettlement settle()
  {
    isSettled = true;
    return this;
  }

  public void addAmount(BigDecimal amount) {
    this.settlementAmount = settlementAmount.add(amount);
  }

  public void subtractAmount(BigDecimal amount) {
    this.settlementAmount = settlementAmount.subtract(amount);
  }


  public FinalSettlementResponse toResponse() {
    return new FinalSettlementResponse(paidUser.getId(),
      borrowedUser.getId(),
      activity.getId(),
      settlementAmount,
      isSettled
      );
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    FinalSettlement that = (FinalSettlement) o;
    return Objects.equals(id, that.id) &&
           Objects.equals(paidUser.getId(), that.paidUser.getId()) &&
           Objects.equals(borrowedUser.getId(), that.borrowedUser.getId()) &&
           Objects.equals(activity.getId(), that.activity.getId()) &&
           Objects.equals(settlementAmount, that.settlementAmount) &&
           Objects.equals(isSettled, that.isSettled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, paidUser, borrowedUser, activity, settlementAmount, isSettled);
  }
}
