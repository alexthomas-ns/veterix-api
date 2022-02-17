package com.veterix.api.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public record Pet(UUID id, String name, PetSpecies species, String breed,
                  ZonedDateTime createdTimestamp) {
}
