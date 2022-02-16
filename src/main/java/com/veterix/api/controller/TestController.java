package com.veterix.api.controller;


import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;


@RestController
@RequestMapping(value = "v1/test", produces = {"application/prs.hal-forms+json",MediaType.APPLICATION_JSON_VALUE})
public class TestController {

    public record Node (String name){}

    @GetMapping
    public Mono<EntityModel<Node>> getRoot(){
        TestController controller = methodOn(TestController.class);

        return Mono.just(new Node("hello world"))
                .flatMap(resource -> linkTo(controller.getChild2())
                        .withSelfRel()
                        .andAffordance(controller.getChild(null))
                        .toMono().map(selflink -> EntityModel.of(resource, selflink)));
    }

    @PostMapping("child")
    public Mono<String> getChild(@RequestBody Node content){
       return Mono.just("child") ;
    }

    @GetMapping("child2")
    public Mono<String> getChild2(){
        return Mono.just("child") ;
    }
}
