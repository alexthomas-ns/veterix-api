package com.veterix.api.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public record Appointment(UUID id,UUID account,UUID pet, ZonedDateTime startTime, ZonedDateTime endTime) {
}
