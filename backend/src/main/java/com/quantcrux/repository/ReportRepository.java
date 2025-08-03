package com.quantcrux.repository;

import com.quantcrux.model.Report;
import com.quantcrux.model.ReportStatus;
import com.quantcrux.model.ReportType;
import com.quantcrux.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    
    List<Report> findByUserOrderByCreatedAtDesc(User user);
    
    List<Report> findByUserAndReportType(User user, ReportType reportType);
    
    List<Report> findByUserAndStatus(User user, ReportStatus status);
    
    @Query("SELECT r FROM Report r WHERE r.user = :user AND r.reportName ILIKE %:name%")
    List<Report> findByUserAndReportNameContaining(@Param("user") User user, @Param("name") String name);
    
    @Query("SELECT r FROM Report r WHERE r.status = :status AND r.expiresAt < :cutoffTime")
    List<Report> findExpiredReports(@Param("status") ReportStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.user = :user AND r.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") ReportStatus status);
    
    @Query("SELECT r FROM Report r WHERE r.user = :user AND r.createdAt >= :fromDate ORDER BY r.createdAt DESC")
    List<Report> findByUserAndCreatedAtAfter(@Param("user") User user, @Param("fromDate") LocalDateTime fromDate);
}