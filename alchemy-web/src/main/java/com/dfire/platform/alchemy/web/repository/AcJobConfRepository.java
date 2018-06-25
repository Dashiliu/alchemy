package com.dfire.platform.alchemy.web.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dfire.platform.alchemy.web.domain.AcJobConf;

@Repository
public interface AcJobConfRepository extends JpaRepository<AcJobConf, Long> {

    List<AcJobConf> findByIsValid(int valid, Sort sort);
}
