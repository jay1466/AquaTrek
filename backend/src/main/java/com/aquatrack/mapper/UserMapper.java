package com.aquatrack.mapper;

import com.aquatrack.dto.response.AuthResponse;
import com.aquatrack.dto.response.UserResponse;
import com.aquatrack.entity.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between {@link User} entity and its DTOs.
 *
 * <p>MapStruct generates the implementation at compile time.
 * The {@code componentModel = "spring"} is set globally via the Maven
 * compiler argument in pom.xml, so it does not need to be repeated here.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Mapper
public interface UserMapper {

    /**
     * Converts a {@link User} entity to a {@link UserResponse} DTO.
     *
     * <p>Custom mappings:
     * <ul>
     *   <li>{@code role} → mapped from {@code user.role.name} (the enum value)</li>
     *   <li>{@code roleDisplayName} → mapped from {@code user.role.displayName}</li>
     *   <li>{@code fullName} → concatenation of first and last name</li>
     * </ul>
     * </p>
     */
    @Mapping(source = "role.name",        target = "role")
    @Mapping(source = "role.displayName", target = "roleDisplayName")
    @Mapping(target = "fullName",         expression = "java(user.getFullName())")
    UserResponse toResponse(User user);

    /**
     * Maps a {@link User} to the compact {@link AuthResponse.UserSummary}
     * embedded in the login response.
     */
    @Mapping(source = "role.name",        target = "role")
    @Mapping(source = "role.displayName", target = "roleDisplayName")
    @Mapping(target = "fullName",         expression = "java(user.getFullName())")
    AuthResponse.UserSummary toUserSummary(User user);
}
