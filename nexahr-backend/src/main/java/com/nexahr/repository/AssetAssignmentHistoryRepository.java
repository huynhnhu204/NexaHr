package com.nexahr.repository;

import com.nexahr.entity.AssetAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAssignmentHistoryRepository extends JpaRepository<AssetAssignmentHistory, Long> {
    List<AssetAssignmentHistory> findByAssetIdOrderByAssignedAtDesc(Long assetId);
    Optional<AssetAssignmentHistory> findFirstByAssetIdAndReturnedAtIsNullOrderByAssignedAtDesc(Long assetId);
}
