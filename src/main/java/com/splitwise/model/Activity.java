package com.splitwise.model;

import static com.splitwise.utils.Constants.timeZone;

import java.time.LocalDateTime;
import java.util.Set;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "activities")
@AllArgsConstructor
@NoArgsConstructor
public class Activity {
  @Id
  @Column(name = "id")
  @SequenceGenerator(name = "activity_sequence", sequenceName = "activity_sequence", allocationSize = 1)
  @GeneratedValue(generator = "activity_sequence")
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "created_at")
  @JsonIgnore
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  @JsonIgnore
  private LocalDateTime updatedAt;

  public Activity(String name) {
    this.name = name;
    this.createdAt = LocalDateTime.now(timeZone);
    this.updatedAt = LocalDateTime.now(timeZone);
  }
}
