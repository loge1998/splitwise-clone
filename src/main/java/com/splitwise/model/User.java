package com.splitwise.model;

import static com.splitwise.utils.Constants.timeZone;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.persistence.*;

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

  @Column(name = "email_id")
  private String emailId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public User(String name, String emailId) {
    this.name = name;
    this.emailId = emailId;
    this.createdAt = LocalDateTime.now(timeZone);
    this.updatedAt = LocalDateTime.now(timeZone);
  }
}
