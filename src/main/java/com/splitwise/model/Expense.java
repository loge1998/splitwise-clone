package com.splitwise.model;

import static com.splitwise.utils.Constants.timeZone;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "expenses")
@AllArgsConstructor
@NoArgsConstructor
public class Expense {
  @Id
  @Column(name = "id")
  @SequenceGenerator(name = "expense_sequence", sequenceName = "expense_sequence", allocationSize = 1)
  @GeneratedValue(generator = "expense_sequence")
  private long id;

  @Column(name = "description")
  private String description;

  @Column(name = "total_amount")
  private BigDecimal totalAmount;

  @ManyToOne
  @JoinColumn(name = "user_who_paid", nullable = false)
  private User userWhoPaid;

  @ManyToOne
  @JoinColumn(name = "activity_id", nullable = false)
  private Activity activity;

  @ManyToMany
  @JoinTable(
    name = "user_expense_mapping",
    joinColumns = @JoinColumn(name = "expense_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
  Set<User> borrowedUsers;

  @Column(name = "created_at")
  @JsonIgnore
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @JsonIgnore
  private LocalDateTime updatedAt;

  public Expense(String description,
    User userWhoPaid,
    BigDecimal totalAmount,
    Activity activity,
    Set<User> borrowedUsers) {
    this.description = description;
    this.totalAmount = totalAmount;
    this.userWhoPaid = userWhoPaid;
    this.activity = activity;
    this.borrowedUsers = borrowedUsers;
    this.createdAt = LocalDateTime.now(timeZone);
    this.updatedAt = LocalDateTime.now(timeZone);
  }
}
