package com.ride.mate.repository;

import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.enums.YesNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Driver Vehicle Details Repository
 * Data access layer for driver vehicle operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 22-02-2026    N/A          N/A          Tishan          Updated for DriverProfile entity
 */
@Repository
public interface DriverVehicleDetailsRepository extends JpaRepository<DriverVehicleDetails, Long> {

    List<DriverVehicleDetails> findByDriverProfile(DriverProfile driverProfile);

    List<DriverVehicleDetails> findByDriverProfileId(Long driverProfileId);

    /**
     * Find vehicles by driver profile ID with all document relationships eagerly fetched.
     *
     * @param driverProfileId Driver profile ID
     * @return List of vehicles with documents loaded
     */
    @Query("SELECT DISTINCT v FROM DriverVehicleDetails v " +
           "LEFT JOIN FETCH v.vehicleType " +
           "LEFT JOIN FETCH v.vehicleMake " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH v.vehicleImageDocument1 " +
           "LEFT JOIN FETCH v.vehicleImageDocument2 " +
           "LEFT JOIN FETCH v.vehicleImageDocument3 " +
           "LEFT JOIN FETCH v.vehicleImageDocument4 " +
           "LEFT JOIN FETCH v.registrationCertificateDocument " +
           "LEFT JOIN FETCH v.insuranceDocument1 " +
           "LEFT JOIN FETCH v.insuranceDocument2 " +
           "LEFT JOIN FETCH v.revenueLicenseDocument1 " +
           "LEFT JOIN FETCH v.revenueLicenseDocument2 " +
           "WHERE v.driverProfile.id = :driverProfileId")
    List<DriverVehicleDetails> findByDriverProfileIdWithDocuments(@Param("driverProfileId") Long driverProfileId);

    Optional<DriverVehicleDetails> findByDriverProfileIdAndIsPrimary(Long driverProfileId, YesNo isPrimary);

    Optional<DriverVehicleDetails> findFirstByDriverProfileIdAndIsPrimary(Long driverProfileId, YesNo isPrimary);

    Optional<DriverVehicleDetails> findByDriverProfileAndIsPrimary(DriverProfile driverProfile, YesNo isPrimary);

    Optional<DriverVehicleDetails> findByRegistrationNumber(String registrationNumber);

    List<DriverVehicleDetails> findByDriverProfileIdAndStatus(Long driverProfileId, String status);

    List<DriverVehicleDetails> findByStatus(String status);

    /**
     * Find vehicles by status with all document relationships eagerly fetched.
     * Avoids LazyInitializationException when mapping to response DTOs.
     *
     * @param status Vehicle status
     * @return List of vehicles with documents loaded
     */
    @Query("SELECT DISTINCT v FROM DriverVehicleDetails v " +
           "LEFT JOIN FETCH v.vehicleType " +
           "LEFT JOIN FETCH v.vehicleMake " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH v.vehicleImageDocument1 " +
           "LEFT JOIN FETCH v.vehicleImageDocument2 " +
           "LEFT JOIN FETCH v.vehicleImageDocument3 " +
           "LEFT JOIN FETCH v.vehicleImageDocument4 " +
           "LEFT JOIN FETCH v.registrationCertificateDocument " +
           "LEFT JOIN FETCH v.insuranceDocument1 " +
           "LEFT JOIN FETCH v.insuranceDocument2 " +
           "LEFT JOIN FETCH v.revenueLicenseDocument1 " +
           "LEFT JOIN FETCH v.revenueLicenseDocument2 " +
           "WHERE v.status = :status")
    List<DriverVehicleDetails> findByStatusWithDocuments(@Param("status") String status);

    /**
     * Find a single vehicle by ID with all document relationships eagerly fetched.
     *
     * @param id Vehicle ID
     * @return Optional vehicle with documents loaded
     */
    @Query("SELECT v FROM DriverVehicleDetails v " +
           "LEFT JOIN FETCH v.vehicleType " +
           "LEFT JOIN FETCH v.vehicleMake " +
           "LEFT JOIN FETCH v.vehicleModel " +
           "LEFT JOIN FETCH v.vehicleImageDocument1 " +
           "LEFT JOIN FETCH v.vehicleImageDocument2 " +
           "LEFT JOIN FETCH v.vehicleImageDocument3 " +
           "LEFT JOIN FETCH v.vehicleImageDocument4 " +
           "LEFT JOIN FETCH v.registrationCertificateDocument " +
           "LEFT JOIN FETCH v.insuranceDocument1 " +
           "LEFT JOIN FETCH v.insuranceDocument2 " +
           "LEFT JOIN FETCH v.revenueLicenseDocument1 " +
           "LEFT JOIN FETCH v.revenueLicenseDocument2 " +
           "WHERE v.id = :id")
    Optional<DriverVehicleDetails> findByIdWithDocuments(@Param("id") Long id);

    boolean existsByRegistrationNumber(String registrationNumber);

    long countByDriverProfileId(Long driverProfileId);
}

