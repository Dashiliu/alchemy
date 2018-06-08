package com.dfire.platform.web.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcJobConfRepository extends JpaRepository<AcJobConf, Long> {

}
