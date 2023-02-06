package com.splitwise.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserActivityMappingId implements Serializable {
  @Column(name = "user_id")
  private long userId;
  @Column(name = "activity_id")
  private long activityId;
}
