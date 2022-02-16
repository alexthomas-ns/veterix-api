package com.veterix.api.controller;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "v1",produces = {"application/prs.hal-forms+json"})
public class RootController {
    @GetMapping
    public Mono<EntityModel<Object>> getRoot(){
        WebFluxLinkBuilder.WebFluxBuilder roomBuilder = linkTo(methodOn(RoomController.class).getExaminationRooms());
        Mono<Link> roomsLink = roomBuilder.withRel("examination-rooms").toMono();
        Mono<Link> roomLink = roomsLink.map(link -> Link.of(link.getHref() + "/{roomId}").withRel("room"));
        Mono<Link> accountsLink = linkTo(methodOn(AccountController.class).getAccounts()).withRel("accounts").toMono();
        Mono<Link> accountLink = accountsLink.map(link -> Link.of(link.getHref() + "/{accountId}").withRel("account"));
        return Flux.concat(roomLink,roomsLink,accountsLink,accountLink).collectList()
                .map(links->EntityModel.of(new Object(),links));

    }
}
