package com.dfire.platform.alchemy.web.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dfire.platform.alchemy.web.domain.AcJobHistory;

@SuppressWarnings("JpaQlInspection")
@Repository
public interface AcJobHistoryRepository extends JpaRepository<AcJobHistory, Long> {

    @Modifying
    @Query(value = "update ac_job_history set is_valid=0 where ac_job_id=?1 ", nativeQuery = true)
    @Transactional
    void deleteJobHistory(Long acJobId);

    List<AcJobHistory> findByClusterJobId(String clusterJobId, Pageable pageable);

}
