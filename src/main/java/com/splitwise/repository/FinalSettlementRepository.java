package com.splitwise.repository;

import java.util.List;

import com.splitwise.model.FinalSettlement;

import com.splitwise.model.FinalSettlementId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FinalSettlementRepository extends JpaRepository<FinalSettlement,FinalSettlementId> {
  List<FinalSettlement> findByIdActivityId(Long activityId);
}
