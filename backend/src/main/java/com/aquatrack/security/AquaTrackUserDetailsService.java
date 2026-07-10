package com.aquatrack.security;

import com.aquatrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation for AquaTrack.
 *
 * <p>Loads a {@link com.aquatrack.entity.User} by email address.
 * Since {@link com.aquatrack.entity.User} implements {@link UserDetails},
 * it is returned directly without any mapping.</p>
 *
 * <p>This service is called by the JWT filter on every authenticated request
 * to validate that the email in the token still corresponds to a valid user.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AquaTrackUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their email address.
     *
     * <p>Only non-deleted users are returned. If the user is deleted,
     * a {@link UsernameNotFoundException} is thrown, which Spring Security
     * translates to a 401 Unauthorized response.</p>
     *
     * @param email the user's email address (used as the Spring Security username)
     * @return the matching {@link com.aquatrack.entity.User} as {@link UserDetails}
     * @throws UsernameNotFoundException if no non-deleted user with this email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    log.debug("User not found with email: {}", email);
                    return new UsernameNotFoundException(
                            "User not found with email: " + email);
                });
    }
}
