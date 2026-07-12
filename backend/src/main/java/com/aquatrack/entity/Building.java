package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a building/block/wing within an apartment society.
 *
 * <p>A building belongs to exactly one {@link Apartment} and contains
 * multiple households. It is identified within its apartment by a short
 * code (e.g., "A", "B", "Wing-1").</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building extends BaseEntity {

    /** The apartment society this building belongs to. */
    @Column(name = "apartment_id", nullable = false)
    private UUID apartmentId;

    /** Full display name, e.g. "Tower A" or "Green Block". */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /** Short code used in flat numbers, e.g. "A" → flat "A-101". */
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "total_floors", nullable = false)
    @Builder.Default
    private Integer totalFloors = 1;

    @Column(name = "total_units", nullable = false)
    @Builder.Default
    private Integer totalUnits = 0;

    @Column(name = "description", length = 1000)
    private String description;

    /** RESIDENTIAL | COMMERCIAL | MIXED */
    @Column(name = "building_type", nullable = false, length = 50)
    @Builder.Default
    private String buildingType = "RESIDENTIAL";
}
