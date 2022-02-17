package com.veterix.api.controller;

import com.veterix.api.model.PetSpecies;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "v1/type",produces = {"application/prs.hal-forms+json", MediaType.APPLICATION_JSON_VALUE})
public class TypesController {

    @GetMapping
    public Mono<EntityModel<Object>> getTypes(){
        TypesController typesController = methodOn(TypesController.class);
        Mono<Link> petLink = linkTo(typesController.getPetTypes()).withRel("pet").toMono();
        return Flux.concat(petLink).collectList()
                .map(links->EntityModel.of(new Object(),links));
    }

    @GetMapping("pet")
    public Mono<PetSpecies[]> getPetTypes(){
        return Mono.just(PetSpecies.values());
    }
}
