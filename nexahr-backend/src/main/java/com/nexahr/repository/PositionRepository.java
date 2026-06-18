package com.nexahr.repository;

import com.nexahr.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByCompanyId(Long companyId);
    Optional<Position> findByNameAndCompanyId(String name, Long companyId);
    boolean existsByNameAndCompanyId(String name, Long companyId);
}
