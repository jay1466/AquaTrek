package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents an apartment society — the top-level multi-tenant entity in AquaTrack.
 *
 * <p>Every other domain object (buildings, households, meters, invoices) belongs
 * to exactly one apartment. All queries are scoped by {@code apartment_id}.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apartment extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    // ── Address ───────────────────────────────────────────────

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 20)
    private String pincode;

    @Column(name = "country", nullable = false, length = 100)
    @Builder.Default
    private String country = "India";

    // ── Contact ───────────────────────────────────────────────

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    // ── Stats ─────────────────────────────────────────────────

    @Column(name = "total_units", nullable = false)
    @Builder.Default
    private Integer totalUnits = 0;

    @Column(name = "total_buildings", nullable = false)
    @Builder.Default
    private Integer totalBuildings = 0;

    @Column(name = "established_year")
    private Integer establishedYear;

    // ── Subscription ──────────────────────────────────────────

    @Column(name = "subscription_plan", nullable = false, length = 50)
    @Builder.Default
    private String subscriptionPlan = "BASIC";

    @Column(name = "subscription_valid_until")
    private LocalDateTime subscriptionValidUntil;

    /**
     * Returns the full formatted address.
     *
     * @return single-line address string
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder(addressLine1);
        if (addressLine2 != null && !addressLine2.isBlank()) {
            sb.append(", ").append(addressLine2);
        }
        sb.append(", ").append(city)
          .append(", ").append(state)
          .append(" - ").append(pincode)
          .append(", ").append(country);
        return sb.toString();
    }
}
