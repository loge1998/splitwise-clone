package com.splitwise.repository;

import com.splitwise.model.UserActivityMapping;
import com.splitwise.model.UserActivityMappingId;
import org.springframework.data.repository.CrudRepository;

public interface UserActivityMappingRepository extends CrudRepository<UserActivityMapping,UserActivityMappingId> {
}
