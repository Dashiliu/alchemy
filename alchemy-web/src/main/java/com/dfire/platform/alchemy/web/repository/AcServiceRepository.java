package com.dfire.platform.alchemy.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dfire.platform.alchemy.web.domain.AcService;

@Repository
public interface AcServiceRepository extends JpaRepository<AcService, Long> {

}
