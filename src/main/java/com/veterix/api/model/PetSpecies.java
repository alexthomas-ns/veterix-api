package com.veterix.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
@Getter
public enum PetSpecies {
    DOG("Dog"),CAT("Cat"),OCTOPUS("Octopus"),
    DRAGON("Dragon"),MONKEY("Monkey"), DINOSAUR("Dinosaur");

    private final String name;

    public String getType(){
        return name();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PetSpecies fromMap(Map<Object,Object> map){
        return valueOf((String) map.get("type"));
    }

}
