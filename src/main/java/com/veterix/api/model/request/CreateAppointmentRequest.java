package com.veterix.api.model.request;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(UUID account, UUID pet, ZonedDateTime startTime, ZonedDateTime endTime) {

}
