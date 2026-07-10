package com.aquatrack.enums;

/**
 * Type of notification dispatched by the AquaTrack notification engine.
 * Determines delivery channel and template selection.
 */
public enum NotificationType {

    EMAIL("Email"),
    SMS("SMS"),
    PUSH("Push Notification"),
    IN_APP("In-App Notification");

    private final String displayName;
    NotificationType(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
