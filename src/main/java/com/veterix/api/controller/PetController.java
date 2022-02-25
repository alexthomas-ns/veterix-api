package com.veterix.api.controller;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.veterix.api.commands.account.AddPetCommand;
import com.veterix.api.commands.account.DeletePetCommand;
import com.veterix.api.model.Account;
import com.veterix.api.model.Pet;
import com.veterix.api.model.request.CreatePetRequest;
import com.veterix.api.util.EventUtility;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping(value = "v1/account/{accountId}/pet",produces = {"application/prs.hal-forms+json", MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class PetController {

    private final EventStoreDBClient client;
    private final AccountController accountController;

    @PostMapping
    public Mono<EntityModel<Account>> createPet(@PathVariable UUID accountId, @RequestBody CreatePetRequest createPetRequest){
        AddPetCommand command = new AddPetCommand();
        command.setId(accountId);
        command.setPetId(UUID.randomUUID());
        command.setEventTimestamp(ZonedDateTime.now());
        command.setName(createPetRequest.name());
        command.setSpecies(createPetRequest.species());
        command.setAge(createPetRequest.age());
        command.setBreed(createPetRequest.breed());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(),eventData)).then(accountController.getAccount(accountId));
    }


    @GetMapping
    public Mono<CollectionModel<Pet>> getPets(@PathVariable UUID accountId){
        return accountController.getAccount(accountId)
                .mapNotNull(EntityModel::getContent)
                .map(Account::pets)
                .map(CollectionModel::of);
    }

    @GetMapping("{petId}")
    public Mono<EntityModel<Pet>> getPet(@PathVariable UUID accountId,@PathVariable UUID petId){
        return accountController.getAccount(accountId)
                .mapNotNull(EntityModel::getContent)
                .flatMapIterable(Account::pets)
                .filter(p-> Objects.equals(p.id(), petId))
                .next()
                .map(EntityModel::of);
    }

    @DeleteMapping("{petId}")
    public Mono<Void> deletePet(@PathVariable UUID accountId, @PathVariable UUID petId){
        DeletePetCommand command = new DeletePetCommand();
        command.setId(accountId);
        command.setPetId(petId);
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(),eventData)).then();
    }
}
