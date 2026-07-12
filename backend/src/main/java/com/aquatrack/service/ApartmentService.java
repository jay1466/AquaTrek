package com.aquatrack.service;

import com.aquatrack.dto.request.ApartmentRequest;
import com.aquatrack.dto.response.ApartmentResponse;
import com.aquatrack.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service contract for apartment society management.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public interface ApartmentService {

    ApartmentResponse create(ApartmentRequest request);

    ApartmentResponse getById(UUID id);

    ApartmentResponse getMyApartment();

    PagedResponse<ApartmentResponse> getAll(String keyword, Pageable pageable);

    ApartmentResponse update(UUID id, ApartmentRequest request);

    void delete(UUID id);

    ApartmentResponse activate(UUID id);

    ApartmentResponse deactivate(UUID id);
}
