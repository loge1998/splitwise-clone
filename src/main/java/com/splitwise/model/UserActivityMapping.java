package com.splitwise.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "user_activity_mapping")
@AllArgsConstructor
@NoArgsConstructor
public class UserActivityMapping {
  @EmbeddedId
  private UserActivityMappingId mappingId;
}
