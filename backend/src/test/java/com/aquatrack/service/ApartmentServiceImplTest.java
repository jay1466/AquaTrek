package com.aquatrack.service;

import com.aquatrack.dto.request.ApartmentRequest;
import com.aquatrack.dto.response.ApartmentResponse;
import com.aquatrack.entity.Apartment;
import com.aquatrack.enums.Status;
import com.aquatrack.exception.DuplicateResourceException;
import com.aquatrack.exception.ResourceNotFoundException;
import com.aquatrack.mapper.ApartmentMapper;
import com.aquatrack.repository.ApartmentRepository;
import com.aquatrack.service.impl.ApartmentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ApartmentServiceImpl}.
 *
 * @author AquaTrack Engineering Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApartmentServiceImpl Unit Tests")
class ApartmentServiceImplTest {

    @Mock private ApartmentRepository apartmentRepository;
    @Mock private ApartmentMapper     apartmentMapper;

    @InjectMocks
    private ApartmentServiceImpl service;

    private ApartmentRequest validRequest;
    private Apartment        savedApartment;
    private ApartmentResponse apartmentResponse;
    private UUID              aptId;

    @BeforeEach
    void setUp() {
        aptId = UUID.randomUUID();

        validRequest = ApartmentRequest.builder()
                .name("Green Valley Society")
                .addressLine1("Plot 42")
                .city("Pune")
                .state("Maharashtra")
                .pincode("411015")
                .country("India")
                .totalUnits(120)
                .build();

        savedApartment = Apartment.builder()
                .name("Green Valley Society")
                .addressLine1("Plot 42")
                .city("Pune")
                .state("Maharashtra")
                .pincode("411015")
                .country("India")
                .totalUnits(120)
                .totalBuildings(0)
                .build();
        savedApartment.setStatus(Status.ACTIVE);
        ReflectionTestUtils.setField(savedApartment, "id", aptId);

        apartmentResponse = ApartmentResponse.builder()
                .id(aptId)
                .name("Green Valley Society")
                .city("Pune")
                .state("Maharashtra")
                .status(Status.ACTIVE)
                .build();
    }

    // ── create() ──────────────────────────────────────────────

    @Test
    @DisplayName("create() should persist and return ApartmentResponse on success")
    void create_validRequest_shouldReturnResponse() {
        when(apartmentRepository.existsByNameIgnoreCaseAndIsDeletedFalse("Green Valley Society"))
                .thenReturn(false);
        when(apartmentMapper.toEntity(validRequest)).thenReturn(savedApartment);
        when(apartmentRepository.save(savedApartment)).thenReturn(savedApartment);
        when(apartmentMapper.toResponse(savedApartment)).thenReturn(apartmentResponse);

        ApartmentResponse result = service.create(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Green Valley Society");
        verify(apartmentRepository).save(savedApartment);
    }

    @Test
    @DisplayName("create() should throw DuplicateResourceException when name already exists")
    void create_duplicateName_shouldThrowDuplicateResourceException() {
        when(apartmentRepository.existsByNameIgnoreCaseAndIsDeletedFalse("Green Valley Society"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Apartment already exists");

        verify(apartmentRepository, never()).save(any());
    }

    // ── getById() ─────────────────────────────────────────────

    @Test
    @DisplayName("getById() should return ApartmentResponse when found")
    void getById_existingId_shouldReturnResponse() {
        when(apartmentRepository.findByIdAndIsDeletedFalse(aptId))
                .thenReturn(Optional.of(savedApartment));
        when(apartmentMapper.toResponse(savedApartment)).thenReturn(apartmentResponse);

        // Mock TenantUtils — inject same aptId as current apartment
        try (var tenantMock = mockStatic(com.aquatrack.utility.TenantUtils.class)) {
            tenantMock.when(com.aquatrack.utility.TenantUtils::getCurrentApartmentId)
                    .thenReturn(aptId);

            ApartmentResponse result = service.getById(aptId);
            assertThat(result.getId()).isEqualTo(aptId);
        }
    }

    @Test
    @DisplayName("getById() should throw ResourceNotFoundException for unknown ID")
    void getById_unknownId_shouldThrowResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(apartmentRepository.findByIdAndIsDeletedFalse(unknownId))
                .thenReturn(Optional.empty());

        try (var tenantMock = mockStatic(com.aquatrack.utility.TenantUtils.class)) {
            tenantMock.when(com.aquatrack.utility.TenantUtils::getCurrentApartmentId)
                    .thenReturn(unknownId);

            assertThatThrownBy(() -> service.getById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Apartment not found");
        }
    }

    // ── delete() ──────────────────────────────────────────────

    @Test
    @DisplayName("delete() should soft-delete the apartment")
    void delete_existingId_shouldSoftDelete() {
        when(apartmentRepository.findByIdAndIsDeletedFalse(aptId))
                .thenReturn(Optional.of(savedApartment));
        when(apartmentRepository.save(any())).thenReturn(savedApartment);

        service.delete(aptId);

        assertThat(savedApartment.getIsDeleted()).isTrue();
        verify(apartmentRepository).save(savedApartment);
    }

    @Test
    @DisplayName("delete() should throw ResourceNotFoundException when apartment not found")
    void delete_unknownId_shouldThrow() {
        when(apartmentRepository.findByIdAndIsDeletedFalse(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── activate / deactivate ─────────────────────────────────

    @Test
    @DisplayName("activate() should set status to ACTIVE")
    void activate_shouldSetStatusActive() {
        savedApartment.setStatus(Status.INACTIVE);
        when(apartmentRepository.findByIdAndIsDeletedFalse(aptId))
                .thenReturn(Optional.of(savedApartment));
        when(apartmentRepository.save(savedApartment)).thenReturn(savedApartment);
        when(apartmentMapper.toResponse(savedApartment)).thenReturn(apartmentResponse);

        service.activate(aptId);

        assertThat(savedApartment.getStatus()).isEqualTo(Status.ACTIVE);
    }
}
