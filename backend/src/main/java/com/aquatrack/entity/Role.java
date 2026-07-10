package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import com.aquatrack.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a system role in AquaTrack's RBAC model.
 *
 * <p>Roles are seeded at startup via Flyway and are not user-modifiable.
 * Each {@link User} is assigned exactly one role that governs their
 * permissions across the entire application.</p>
 *
 * <p>Role hierarchy: SUPER_ADMIN > ADMIN > MANAGER > RESIDENT</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    /**
     * The role enum value — stored as a VARCHAR matching the {@link UserRole} enum name.
     * Unique per table row; used for Spring Security authority lookups.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private UserRole name;

    /** Human-readable role label shown in the UI (e.g., "Administrator"). */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** Optional description of what this role can do. */
    @Column(name = "description", length = 500)
    private String description;
}
