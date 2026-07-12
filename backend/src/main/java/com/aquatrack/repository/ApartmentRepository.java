package com.aquatrack.repository;

import com.aquatrack.entity.Apartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Apartment} entity.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    Optional<Apartment> findByIdAndIsDeletedFalse(UUID id);

    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndIdNotAndIsDeletedFalse(String name, UUID id);

    @Query("""
            SELECT a FROM Apartment a
            WHERE a.isDeleted = FALSE
            AND (:keyword IS NULL OR :keyword = ''
                 OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(a.city) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Apartment> searchApartments(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Apartment a WHERE a.isDeleted = FALSE AND a.status = 'ACTIVE'")
    long countActive();

    /** Updates denormalised building count on the apartment record. */
    @Modifying
    @Query("UPDATE Apartment a SET a.totalBuildings = :count WHERE a.id = :id")
    void updateBuildingCount(@Param("id") UUID id, @Param("count") int count);

    /** Updates denormalised unit count on the apartment record. */
    @Modifying
    @Query("UPDATE Apartment a SET a.totalUnits = :count WHERE a.id = :id")
    void updateUnitCount(@Param("id") UUID id, @Param("count") int count);
}
