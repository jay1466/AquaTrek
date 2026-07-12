package com.aquatrack.mapper;

import com.aquatrack.dto.request.BuildingRequest;
import com.aquatrack.dto.response.BuildingResponse;
import com.aquatrack.entity.Building;
import org.mapstruct.*;

/**
 * MapStruct mapper for {@link Building}.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Mapper(builder = @org.mapstruct.Builder(disableBuilder = true))
public interface BuildingMapper {

    BuildingResponse toResponse(Building building);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "apartmentId",  ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "version",      ignore = true)
    @Mapping(target = "isDeleted",    ignore = true)
    @Mapping(target = "status",       ignore = true)
    Building toEntity(BuildingRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "apartmentId",  ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "updatedBy",    ignore = true)
    @Mapping(target = "version",      ignore = true)
    @Mapping(target = "isDeleted",    ignore = true)
    @Mapping(target = "status",       ignore = true)
    void updateEntityFromRequest(BuildingRequest request, @MappingTarget Building building);
}
