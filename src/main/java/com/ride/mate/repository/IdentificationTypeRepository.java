package com.ride.mate.repository;

import com.ride.mate.domain.IdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Identification Type Repository
 * Data access layer for identification type operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface IdentificationTypeRepository extends JpaRepository<IdentificationType, Long> {

    Optional<IdentificationType> findByTypeCode(String typeCode);
}

