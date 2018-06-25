package com.dfire.platform.alchemy.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dfire.platform.alchemy.web.domain.AcJobFailMsg;

@Repository
public interface AcJobFailMsgRepository extends JpaRepository<AcJobFailMsg, Long> {

}
