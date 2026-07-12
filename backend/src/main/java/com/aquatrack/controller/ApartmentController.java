package com.aquatrack.controller;

import com.aquatrack.constants.ApiConstants;
import com.aquatrack.dto.request.ApartmentRequest;
import com.aquatrack.dto.response.ApartmentResponse;
import com.aquatrack.response.ApiResponse;
import com.aquatrack.response.PagedResponse;
import com.aquatrack.service.ApartmentService;
import com.aquatrack.utility.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for apartment society management.
 * Base path: {@code /api/v1/apartments}
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@RestController
@RequestMapping(ApiConstants.APARTMENTS_BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Apartments", description = "Apartment society management")
@SecurityRequirement(name = "bearerAuth")
public class ApartmentController {

    private final ApartmentService apartmentService;

    @PostMapping
    @Operation(summary = "Create a new apartment society (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<ApartmentResponse>> create(
            @Valid @RequestBody ApartmentRequest request) {
        return ResponseUtils.created(apartmentService.create(request),
                "Apartment society created successfully.");
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user's apartment society")
    public ResponseEntity<ApiResponse<ApartmentResponse>> getMyApartment() {
        return ResponseUtils.ok(apartmentService.getMyApartment());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get apartment by ID")
    public ResponseEntity<ApiResponse<ApartmentResponse>> getById(@PathVariable UUID id) {
        return ResponseUtils.ok(apartmentService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all apartments (SUPER_ADMIN only) with search and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ApartmentResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseUtils.ok(apartmentService.getAll(keyword, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update apartment details")
    public ResponseEntity<ApiResponse<ApartmentResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ApartmentRequest request) {
        return ResponseUtils.ok(apartmentService.update(id, request),
                "Apartment updated successfully.");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an apartment (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable UUID id) {
        apartmentService.delete(id);
        return ResponseUtils.ok("Apartment deleted successfully.");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a suspended apartment")
    public ResponseEntity<ApiResponse<ApartmentResponse>> activate(@PathVariable UUID id) {
        return ResponseUtils.ok(apartmentService.activate(id), "Apartment activated.");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an apartment")
    public ResponseEntity<ApiResponse<ApartmentResponse>> deactivate(@PathVariable UUID id) {
        return ResponseUtils.ok(apartmentService.deactivate(id), "Apartment deactivated.");
    }
}
