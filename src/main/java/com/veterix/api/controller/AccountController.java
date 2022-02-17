package com.veterix.api.controller;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.veterix.api.collector.AccountCollector;
import com.veterix.api.commands.account.AccountContext;
import com.veterix.api.commands.account.AccountDeletedCommand;
import com.veterix.api.commands.account.AccountUpdateCommand;
import com.veterix.api.commands.account.CreateAccountCommand;
import com.veterix.api.commands.room.ExaminationRoomContext;
import com.veterix.api.commands.room.ExaminationRoomDeletedCommand;
import com.veterix.api.commands.room.ExaminationRoomUpdateCommand;
import com.veterix.api.exception.room.AccountNotFound;
import com.veterix.api.exception.room.ExaminationRoomNotFound;
import com.veterix.api.model.Account;
import com.veterix.api.model.ExaminationRoom;
import com.veterix.api.model.request.CreateAccountRequest;
import com.veterix.api.model.request.CreateExaminationRoomRequest;
import com.veterix.api.util.EventUtility;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "v1/account",produces = {"application/prs.hal-forms+json", MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AccountController {

    private final EventStoreDBClient client;
    private final AccountCollector collector;

    @PostMapping
    @SneakyThrows
    public Mono<EntityModel<Account>> createAccount(@RequestBody CreateAccountRequest request){
        CreateAccountCommand command = new CreateAccountCommand();
        BeanUtils.copyProperties(request,command);
        command.setId(UUID.randomUUID());
        command.setEventTimestamp(ZonedDateTime.now());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(), eventData)).then(getAccount(command.getId()));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<Account>>> getExaminationRoomResponse(){
        AccountController accountController = methodOn(AccountController.class);
         return getAccounts().flatMap(
                e -> linkTo(accountController.getAccount(e.id()))
                        .withSelfRel()
//                        .andAffordance(roomController.updateExaminationRoom(e.id(),null)) todo: write update method
                        .andAffordance(accountController.getAccount(e.id()))
                        .toMono().map(selfLink -> EntityModel.of(e, selfLink))
        ).collectList().map(CollectionModel::of);
    }

    public Flux<Account> getAccounts(){
        AccountContext context = new AccountContext();
        return collector.getCommands()
                .transform(context::acceptCommand)
                .flatMapIterable(Map::values);
    }


    @GetMapping("{accountId}")
    public Mono<EntityModel<Account>> getAccount(@PathVariable UUID accountId){
        AccountController accountController = methodOn(AccountController.class);
        Mono<Link> allLink = linkTo(accountController.getExaminationRoomResponse())
                .withRel("all").toMono();
        PetController petController = methodOn(PetController.class);
        Mono<Link> petLink = linkTo(petController.getPets(accountId)).withRel("pets")
                .andAffordance(petController.createPet(accountId,null))
                .toMono();
        return getAccounts()
                .filter(r->r.id().equals(accountId))
                .next()
                .switchIfEmpty(Mono.error(new AccountNotFound(accountId)))
                .flatMap(e->
                        linkTo(accountController.getAccount(accountId))
                                .withSelfRel()
                                .andAffordance(accountController.updateAccount(accountId,null))
                                .toMono().map(link -> EntityModel.of(e,link))
                                .zipWith(allLink, RepresentationModel::add)
                                .zipWith(petLink, RepresentationModel::add)

                );
    }

    @PutMapping("{accountId}")
    public Mono<EntityModel<Account>> updateAccount(@PathVariable UUID accountId, @RequestBody CreateAccountRequest account){
        AccountUpdateCommand command = new AccountUpdateCommand();
        BeanUtils.copyProperties(account,command);
        command.setId(accountId);
        command.setEventTimestamp(ZonedDateTime.now());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(), eventData)).flatMap((result)-> getAccount(accountId));
    }

    @DeleteMapping("{accountId}")
    public Mono<Void> updateExaminationRoom(@PathVariable UUID accountId) {
        AccountDeletedCommand command = new AccountDeletedCommand();
        command.setId(accountId);
        command.setEventTimestamp(ZonedDateTime.now());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(), eventData)).then();
    }
}
