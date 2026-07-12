package com.aquatrack.repository;

import com.aquatrack.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Building} entity.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    Optional<Building> findByIdAndApartmentIdAndIsDeletedFalse(UUID id, UUID apartmentId);

    List<Building> findByApartmentIdAndIsDeletedFalseOrderByNameAsc(UUID apartmentId);

    boolean existsByApartmentIdAndCodeIgnoreCaseAndIsDeletedFalse(UUID apartmentId, String code);

    boolean existsByApartmentIdAndCodeIgnoreCaseAndIdNotAndIsDeletedFalse(
            UUID apartmentId, String code, UUID id);

    @Query("""
            SELECT b FROM Building b
            WHERE b.apartmentId = :apartmentId
              AND b.isDeleted = FALSE
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Building> searchByApartment(
            @Param("apartmentId") UUID apartmentId,
            @Param("keyword") String keyword,
            Pageable pageable);

    long countByApartmentIdAndIsDeletedFalse(UUID apartmentId);
}
