package com.aquatrack.service.impl;

import com.aquatrack.dto.request.BuildingRequest;
import com.aquatrack.dto.response.BuildingResponse;
import com.aquatrack.entity.Building;
import com.aquatrack.exception.*;
import com.aquatrack.mapper.BuildingMapper;
import com.aquatrack.repository.ApartmentRepository;
import com.aquatrack.repository.BuildingRepository;
import com.aquatrack.response.PagedResponse;
import com.aquatrack.service.BuildingService;
import com.aquatrack.utility.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link BuildingService}.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository   buildingRepository;
    private final ApartmentRepository  apartmentRepository;
    private final BuildingMapper       buildingMapper;

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public BuildingResponse create(BuildingRequest request) {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        log.info("Creating building '{}' in apartment [{}]", request.getName(), apartmentId);

        // Validate apartment exists
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new ResourceNotFoundException("Apartment", "id", apartmentId);
        }

        // Validate code uniqueness within apartment
        if (buildingRepository.existsByApartmentIdAndCodeIgnoreCaseAndIsDeletedFalse(
                apartmentId, request.getCode())) {
            throw new DuplicateResourceException("Building", "code", request.getCode());
        }

        Building building = buildingMapper.toEntity(request);
        building.setApartmentId(apartmentId);
        if (building.getBuildingType() == null) building.setBuildingType("RESIDENTIAL");

        Building saved = buildingRepository.save(building);

        // Update denormalised count on apartment
        long count = buildingRepository.countByApartmentIdAndIsDeletedFalse(apartmentId);
        apartmentRepository.updateBuildingCount(apartmentId, (int) count);

        log.info("Building created: {} [id={}]", saved.getName(), saved.getId());
        return buildingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public BuildingResponse getById(UUID id) {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        Building building = buildingRepository
                .findByIdAndApartmentIdAndIsDeletedFalse(id, apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        return buildingMapper.toResponse(building);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<BuildingResponse> getAllForCurrentApartment() {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        return buildingRepository
                .findByApartmentIdAndIsDeletedFalseOrderByNameAsc(apartmentId)
                .stream()
                .map(buildingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<BuildingResponse> search(String keyword, Pageable pageable) {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        Page<Building> page = buildingRepository.searchByApartment(apartmentId, keyword, pageable);
        return PagedResponse.from(page.map(buildingMapper::toResponse));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public BuildingResponse update(UUID id, BuildingRequest request) {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        Building building = buildingRepository
                .findByIdAndApartmentIdAndIsDeletedFalse(id, apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        if (!building.getCode().equalsIgnoreCase(request.getCode()) &&
            buildingRepository.existsByApartmentIdAndCodeIgnoreCaseAndIdNotAndIsDeletedFalse(
                    apartmentId, request.getCode(), id)) {
            throw new DuplicateResourceException("Building", "code", request.getCode());
        }

        buildingMapper.updateEntityFromRequest(request, building);
        Building saved = buildingRepository.save(building);
        log.info("Building updated: {} [id={}]", saved.getName(), saved.getId());
        return buildingMapper.toResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public void delete(UUID id) {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        Building building = buildingRepository
                .findByIdAndApartmentIdAndIsDeletedFalse(id, apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));

        building.softDelete();
        buildingRepository.save(building);

        long count = buildingRepository.countByApartmentIdAndIsDeletedFalse(apartmentId);
        apartmentRepository.updateBuildingCount(apartmentId, (int) count);
        log.info("Building soft-deleted [id={}]", id);
    }
}
