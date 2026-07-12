package com.aquatrack.service.impl;

import com.aquatrack.dto.request.ApartmentRequest;
import com.aquatrack.dto.response.ApartmentResponse;
import com.aquatrack.entity.Apartment;
import com.aquatrack.enums.Status;
import com.aquatrack.exception.*;
import com.aquatrack.mapper.ApartmentMapper;
import com.aquatrack.repository.ApartmentRepository;
import com.aquatrack.response.PagedResponse;
import com.aquatrack.service.ApartmentService;
import com.aquatrack.utility.TenantUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link ApartmentService}.
 *
 * <p>Multi-tenancy enforcement:
 * <ul>
 *   <li>SUPER_ADMIN can access all apartments.</li>
 *   <li>ADMIN can only access/modify their own apartment.</li>
 *   <li>Residents have no access to apartment management.</li>
 * </ul>
 * </p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper     apartmentMapper;

    /** Creates a new apartment society. SUPER_ADMIN only. */
    @Override
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ApartmentResponse create(ApartmentRequest request) {
        log.info("Creating apartment: {}", request.getName());

        if (apartmentRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.getName())) {
            throw new DuplicateResourceException("Apartment", "name", request.getName());
        }

        Apartment apartment = apartmentMapper.toEntity(request);
        apartment.setStatus(Status.ACTIVE);
        if (apartment.getCountry() == null || apartment.getCountry().isBlank()) {
            apartment.setCountry("India");
        }

        Apartment saved = apartmentRepository.save(apartment);
        log.info("Apartment created: {} [id={}]", saved.getName(), saved.getId());
        return apartmentMapper.toResponse(saved);
    }

    /** Gets an apartment by ID. SUPER_ADMIN sees any; ADMIN sees only their own. */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ApartmentResponse getById(UUID id) {
        Apartment apartment = findAndValidateAccess(id);
        return apartmentMapper.toResponse(apartment);
    }

    /** Returns the apartment for the currently authenticated user's tenant. */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ApartmentResponse getMyApartment() {
        UUID apartmentId = TenantUtils.getCurrentApartmentId();
        Apartment apartment = apartmentRepository.findByIdAndIsDeletedFalse(apartmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", apartmentId));
        return apartmentMapper.toResponse(apartment);
    }

    /** Lists all apartments (SUPER_ADMIN) with search and pagination. */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PagedResponse<ApartmentResponse> getAll(String keyword, Pageable pageable) {
        Page<Apartment> page = apartmentRepository.searchApartments(keyword, pageable);
        return PagedResponse.from(page.map(apartmentMapper::toResponse));
    }

    /** Updates an apartment's details. */
    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ApartmentResponse update(UUID id, ApartmentRequest request) {
        Apartment apartment = findAndValidateAccess(id);

        // Check name uniqueness if it changed
        if (!apartment.getName().equalsIgnoreCase(request.getName()) &&
            apartmentRepository.existsByNameIgnoreCaseAndIdNotAndIsDeletedFalse(request.getName(), id)) {
            throw new DuplicateResourceException("Apartment", "name", request.getName());
        }

        apartmentMapper.updateEntityFromRequest(request, apartment);
        Apartment saved = apartmentRepository.save(apartment);
        log.info("Apartment updated: {} [id={}]", saved.getName(), saved.getId());
        return apartmentMapper.toResponse(saved);
    }

    /** Soft-deletes an apartment. SUPER_ADMIN only. */
    @Override
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public void delete(UUID id) {
        Apartment apartment = findByIdOrThrow(id);
        apartment.softDelete();
        apartmentRepository.save(apartment);
        log.info("Apartment soft-deleted: [id={}]", id);
    }

    /** Reactivates a suspended or inactive apartment. */
    @Override
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ApartmentResponse activate(UUID id) {
        Apartment apartment = findByIdOrThrow(id);
        apartment.activate();
        return apartmentMapper.toResponse(apartmentRepository.save(apartment));
    }

    /** Deactivates an active apartment. */
    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ApartmentResponse deactivate(UUID id) {
        Apartment apartment = findAndValidateAccess(id);
        apartment.deactivate();
        return apartmentMapper.toResponse(apartmentRepository.save(apartment));
    }

    // ── Private helpers ───────────────────────────────────────

    /** Finds an apartment and validates the caller's tenant access. */
    private Apartment findAndValidateAccess(UUID id) {
        Apartment apartment = findByIdOrThrow(id);
        // If not SUPER_ADMIN, ensure the apartment matches the caller's tenant
        try {
            UUID callerApartmentId = TenantUtils.getCurrentApartmentId();
            if (!callerApartmentId.equals(id)) {
                throw new TenantAccessException();
            }
        } catch (UnauthorizedException e) {
            // SUPER_ADMIN has no apartment — allowed to access any
        }
        return apartment;
    }

    private Apartment findByIdOrThrow(UUID id) {
        return apartmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment", "id", id));
    }
}
