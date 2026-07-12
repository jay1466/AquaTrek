package com.aquatrack.controller;

import com.aquatrack.constants.ApiConstants;
import com.aquatrack.dto.request.BuildingRequest;
import com.aquatrack.dto.response.BuildingResponse;
import com.aquatrack.response.ApiResponse;
import com.aquatrack.response.PagedResponse;
import com.aquatrack.service.BuildingService;
import com.aquatrack.utility.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for building management within an apartment society.
 * Base path: {@code /api/v1/buildings}
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@RestController
@RequestMapping(ApiConstants.BUILDINGS_BASE)
@RequiredArgsConstructor
@Tag(name = "Buildings", description = "Building/block management within an apartment society")
@SecurityRequirement(name = "bearerAuth")
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping
    @Operation(summary = "Create a new building in the current apartment")
    public ResponseEntity<ApiResponse<BuildingResponse>> create(
            @Valid @RequestBody BuildingRequest request) {
        return ResponseUtils.created(buildingService.create(request), "Building created successfully.");
    }

    @GetMapping
    @Operation(summary = "List all buildings in the current apartment (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<BuildingResponse>>> search(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseUtils.ok(buildingService.search(keyword, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all buildings in the current apartment (no pagination — for dropdowns)")
    public ResponseEntity<ApiResponse<List<BuildingResponse>>> getAll() {
        return ResponseUtils.ok(buildingService.getAllForCurrentApartment());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get building by ID")
    public ResponseEntity<ApiResponse<BuildingResponse>> getById(@PathVariable UUID id) {
        return ResponseUtils.ok(buildingService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a building")
    public ResponseEntity<ApiResponse<BuildingResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BuildingRequest request) {
        return ResponseUtils.ok(buildingService.update(id, request), "Building updated successfully.");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a building")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        buildingService.delete(id);
        return ResponseUtils.ok("Building deleted successfully.");
    }
}
