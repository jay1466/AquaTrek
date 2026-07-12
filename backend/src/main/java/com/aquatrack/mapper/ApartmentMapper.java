package com.aquatrack.mapper;

import com.aquatrack.dto.request.ApartmentRequest;
import com.aquatrack.dto.response.ApartmentResponse;
import com.aquatrack.entity.Apartment;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link Apartment}.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface ApartmentMapper {

    @Mapping(target = "fullAddress", expression = "java(apartment.getFullAddress())")
    ApartmentResponse toResponse(Apartment apartment);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "totalBuildings",  ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "subscriptionValidUntil", ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    @Mapping(target = "version",         ignore = true)
    @Mapping(target = "isDeleted",       ignore = true)
    @Mapping(target = "status",          ignore = true)
    Apartment toEntity(ApartmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "totalBuildings",  ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "subscriptionValidUntil", ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "updatedAt",       ignore = true)
    @Mapping(target = "createdBy",       ignore = true)
    @Mapping(target = "updatedBy",       ignore = true)
    @Mapping(target = "version",         ignore = true)
    @Mapping(target = "isDeleted",       ignore = true)
    @Mapping(target = "status",          ignore = true)
    void updateEntityFromRequest(ApartmentRequest request, @MappingTarget Apartment apartment);
}
