package com.ride.mate.repository;

import com.ride.mate.domain.DriverWalletTransaction;
import com.ride.mate.enums.WalletTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * DriverWalletTransactionRepository
 * Data access layer for DriverWalletTransaction entity
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface DriverWalletTransactionRepository extends JpaRepository<DriverWalletTransaction, Long> {

    List<DriverWalletTransaction> findByDriverWalletIdOrderByCreatedDateDesc(Long driverWalletId);

    List<DriverWalletTransaction> findByDriverWalletIdAndTransactionTypeOrderByCreatedDateDesc(
            Long driverWalletId, WalletTransactionType transactionType);

    List<DriverWalletTransaction> findByDriverWalletDriverProfileIdOrderByCreatedDateDesc(Long driverProfileId);

    List<DriverWalletTransaction> findByDriverWalletDriverProfileIdAndTransactionTypeOrderByCreatedDateDesc(
            Long driverProfileId, WalletTransactionType transactionType);

    @Query("SELECT COALESCE(SUM(t.netAmount), 0) FROM DriverWalletTransaction t " +
            "WHERE t.driverWallet.id = :walletId AND t.transactionType = :type")
    BigDecimal sumNetAmountByWalletIdAndType(@Param("walletId") Long walletId,
                                             @Param("type") WalletTransactionType type);

    @Query("SELECT COALESCE(SUM(t.commissionAmount), 0) FROM DriverWalletTransaction t " +
            "WHERE t.driverWallet.id = :walletId AND t.transactionType = 'RIDE_EARNING'")
    BigDecimal sumCommissionByWalletId(@Param("walletId") Long walletId);
}

