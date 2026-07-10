package com.aquatrack.entity;

import com.aquatrack.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit record of each login attempt (successful or failed).
 * Append-only table — records are never updated.
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Entity
@Table(name = "user_login_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginAttempt extends BaseEntity {

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = Boolean.FALSE;

    /** Reason for failure: INVALID_PASSWORD | ACCOUNT_LOCKED | EMAIL_NOT_VERIFIED | USER_NOT_FOUND */
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "attempted_at", nullable = false)
    @Builder.Default
    private LocalDateTime attemptedAt = LocalDateTime.now();
}
