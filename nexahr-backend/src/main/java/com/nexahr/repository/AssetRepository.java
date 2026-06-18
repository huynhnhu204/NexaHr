package com.nexahr.repository;

import com.nexahr.entity.Asset;
import com.nexahr.entity.enums.AssetStatus;
import com.nexahr.entity.enums.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetCode(String assetCode);
    boolean existsByAssetCode(String assetCode);

    @Query("SELECT a FROM Asset a WHERE " +
           "(:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:type IS NULL OR a.assetType = :type) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Asset> findWithFilters(@Param("search") String search,
                                @Param("type") AssetType type,
                                @Param("status") AssetStatus status,
                                Pageable pageable);
}
