package com.aquatrack.enums;

/** Gender options for resident profiles. */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer Not to Say");

    private final String displayName;

    Gender(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
