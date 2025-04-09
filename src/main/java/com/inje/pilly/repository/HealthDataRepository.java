package com.inje.pilly.repository;

import com.inje.pilly.entity.HealthData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthDataRepository extends JpaRepository<HealthData,Long> {
    List<HealthData> findByUser_UserIdOrderByRecordDateAsc(Long userId);
    long countByUser_UserId(Long userId);

    Optional<HealthData> findTopByUser_UserIdOrderByRecordDateDesc(Long userId);
}
