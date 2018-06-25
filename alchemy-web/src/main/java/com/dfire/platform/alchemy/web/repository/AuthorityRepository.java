package com.dfire.platform.alchemy.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dfire.platform.alchemy.web.domain.Authority;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
