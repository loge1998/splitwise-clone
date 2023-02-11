package com.splitwise.repository;

import java.util.List;

import com.splitwise.model.Expense;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
  List<Expense> findAllByActivityId(long ActivityId);
}
