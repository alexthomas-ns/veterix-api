package com.veterix.api.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public record Pet(UUID id, String name, String species, String breed,
                  ZonedDateTime createdTimestamp) {
}
