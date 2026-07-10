package com.aquatrack.repository;

import com.aquatrack.entity.Role;
import com.aquatrack.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Role} entity.
 * Roles are seeded by Flyway and looked up by name during registration.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Finds an active role by its enum name.
     * Used during user registration to assign the correct role.
     *
     * @param name the role enum value
     * @return the Role if it exists and is active
     */
    Optional<Role> findByNameAndIsDeletedFalse(UserRole name);
}
