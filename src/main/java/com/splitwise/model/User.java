package com.splitwise.model;

import static com.splitwise.utils.Constants.timeZone;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
  @Id
  @Column(name = "id")
  @SequenceGenerator(name = "user_sequence", sequenceName = "user_sequence", allocationSize = 1)
  @GeneratedValue(generator = "user_sequence")
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "email_id", unique = true)
  private String emailId;

  @ManyToMany(mappedBy = "borrowedUsers")
  @JsonIgnore
  Set<Expense> expenses;

  @ManyToMany(mappedBy = "users")
  @JsonIgnore
  Set<Activity> activities;

  @Column(name = "created_at")
  @JsonIgnore
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @JsonIgnore
  private LocalDateTime updatedAt;

  public User(String name, String emailId) {
    this.name = name;
    this.emailId = emailId;
    this.createdAt = LocalDateTime.now(timeZone);
    this.updatedAt = LocalDateTime.now(timeZone);
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return id == user.id && name.equals(user.name) && emailId.equals(user.emailId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, emailId);
  }
}
