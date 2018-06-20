package com.dfire.platform.web.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("JpaQlInspection")
@Repository
public interface AcJobHistoryRepository extends JpaRepository<AcJobHistory, Long> {

    @Modifying
    @Query(value = "update ac_job_history set is_valid=0 where ac_job_id=?1 ", nativeQuery = true)
    @Transactional
    void deleteJobHistory(Long acJobId);

    AcJobHistory findByClusterJobId(String clusterJobId, Pageable pageable);

}
