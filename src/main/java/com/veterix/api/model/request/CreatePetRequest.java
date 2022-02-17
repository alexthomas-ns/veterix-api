package com.veterix.api.model.request;

import com.veterix.api.model.PetSpecies;

public record CreatePetRequest(String name, PetSpecies species, String breed, int age) {
}
