package com.splitwise.repository;

import org.springframework.data.repository.CrudRepository;

import com.splitwise.model.Activity;

public interface ActivityRepository extends CrudRepository<Activity,Long> {
}
