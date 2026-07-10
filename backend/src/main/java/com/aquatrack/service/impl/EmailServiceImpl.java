package com.aquatrack.service.impl;

import com.aquatrack.entity.User;
import com.aquatrack.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Asynchronous email service implementation using Spring Mail + Thymeleaf templates.
 *
 * <p>All methods are annotated with {@code @Async} so email sending never
 * blocks the main request thread. Failures are logged but not rethrown —
 * email delivery is best-effort in the current implementation.</p>
 *
 * <p>Templates are Thymeleaf HTML files located in:
 * {@code src/main/resources/templates/email/}</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from-address}")
    private String fromAddress;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.email.base-url}")
    private String appBaseUrl;

    // ── Public API ────────────────────────────────────────────

    /**
     * Sends an email verification link.
     * Link format: {@code {frontendUrl}/verify-email?token={token}}
     */
    @Async("taskExecutor")
    @Override
    public void sendVerificationEmail(User user, String token) {
        String verifyLink = frontendUrl + "/verify-email?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("firstName",  user.getFirstName());
        ctx.setVariable("verifyLink", verifyLink);
        ctx.setVariable("expiryHours", 48);
        ctx.setVariable("appName",    "AquaTrack");
        ctx.setVariable("supportEmail", "support@aquatrack.com");

        sendHtmlEmail(
                user.getEmail(),
                "Verify Your AquaTrack Account",
                "email/verify-email",
                ctx
        );
        log.info("Verification email dispatched to: {}", user.getEmail());
    }

    /**
     * Sends a password reset link.
     * Link format: {@code {frontendUrl}/reset-password?token={token}}
     */
    @Async("taskExecutor")
    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("firstName",   user.getFirstName());
        ctx.setVariable("resetLink",   resetLink);
        ctx.setVariable("expiryHours", 24);
        ctx.setVariable("requestTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        ctx.setVariable("appName",    "AquaTrack");
        ctx.setVariable("supportEmail", "support@aquatrack.com");

        sendHtmlEmail(
                user.getEmail(),
                "Reset Your AquaTrack Password",
                "email/reset-password",
                ctx
        );
        log.info("Password reset email dispatched to: {}", user.getEmail());
    }

    /**
     * Sends a security notification after a password change.
     */
    @Async("taskExecutor")
    @Override
    public void sendPasswordChangedNotification(User user) {
        Context ctx = new Context();
        ctx.setVariable("firstName",    user.getFirstName());
        ctx.setVariable("changedTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        ctx.setVariable("resetLink",    frontendUrl + "/forgot-password");
        ctx.setVariable("appName",      "AquaTrack");
        ctx.setVariable("supportEmail", "support@aquatrack.com");

        sendHtmlEmail(
                user.getEmail(),
                "Your AquaTrack Password Has Been Changed",
                "email/password-changed",
                ctx
        );
        log.info("Password change notification sent to: {}", user.getEmail());
    }

    /**
     * Sends a welcome email after account activation.
     */
    @Async("taskExecutor")
    @Override
    public void sendWelcomeEmail(User user) {
        Context ctx = new Context();
        ctx.setVariable("firstName",   user.getFirstName());
        ctx.setVariable("email",       user.getEmail());
        ctx.setVariable("dashboardUrl", frontendUrl + "/dashboard");
        ctx.setVariable("appName",     "AquaTrack");
        ctx.setVariable("supportEmail", "support@aquatrack.com");

        sendHtmlEmail(
                user.getEmail(),
                "Welcome to AquaTrack!",
                "email/welcome",
                ctx
        );
        log.info("Welcome email sent to: {}", user.getEmail());
    }

    /**
     * Notifies the user that their account has been locked.
     */
    @Async("taskExecutor")
    @Override
    public void sendAccountLockedNotification(User user, int lockDuration) {
        Context ctx = new Context();
        ctx.setVariable("firstName",    user.getFirstName());
        ctx.setVariable("lockDuration", lockDuration);
        ctx.setVariable("resetLink",    frontendUrl + "/forgot-password");
        ctx.setVariable("appName",      "AquaTrack");
        ctx.setVariable("supportEmail", "support@aquatrack.com");

        sendHtmlEmail(
                user.getEmail(),
                "AquaTrack Account Temporarily Locked",
                "email/account-locked",
                ctx
        );
        log.info("Account locked notification sent to: {}", user.getEmail());
    }

    // ── Private Helpers ───────────────────────────────────────

    /**
     * Builds and sends an HTML MIME email using the specified Thymeleaf template.
     *
     * @param to           recipient email address
     * @param subject      email subject line
     * @param templateName path to the Thymeleaf template (without .html extension)
     * @param context      Thymeleaf context with template variables
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            // Add common variables to every template context
            context.setVariable("currentYear",
                    String.valueOf(LocalDateTime.now().getYear()));

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);   // true = isHtml

            mailSender.send(message);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // Log the failure but do not rethrow — email is best-effort
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage(), e);
        }
    }
}
