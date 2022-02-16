package com.veterix.api.model;

import java.time.ZonedDateTime;
import java.util.UUID;


public record ExaminationRoom(
        UUID id,
        String roomNumber,
        ZonedDateTime createdDateTime,
        ZonedDateTime updatedDateTime
) {
}

