package com.aquatrack.service;

import com.aquatrack.dto.request.BuildingRequest;
import com.aquatrack.dto.response.BuildingResponse;
import com.aquatrack.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for building management within an apartment society.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
public interface BuildingService {
    BuildingResponse create(BuildingRequest request);
    BuildingResponse getById(UUID id);
    List<BuildingResponse> getAllForCurrentApartment();
    PagedResponse<BuildingResponse> search(String keyword, Pageable pageable);
    BuildingResponse update(UUID id, BuildingRequest request);
    void delete(UUID id);
}
