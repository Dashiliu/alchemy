package com.dfire.platform.web.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcJobRepository extends JpaRepository<AcJob, Long> {

}
