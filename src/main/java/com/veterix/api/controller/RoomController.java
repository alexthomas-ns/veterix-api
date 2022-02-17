package com.veterix.api.controller;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.veterix.api.collector.ExaminationRoomCollector;
import com.veterix.api.commands.room.ExaminationRoomContext;
import com.veterix.api.commands.room.ExaminationRoomCreatedCommand;
import com.veterix.api.commands.room.ExaminationRoomDeletedCommand;
import com.veterix.api.commands.room.ExaminationRoomUpdateCommand;
import com.veterix.api.exception.room.ExaminationRoomNotFound;
import com.veterix.api.model.ExaminationRoom;
import com.veterix.api.model.request.CreateExaminationRoomRequest;
import com.veterix.api.util.EventUtility;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
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
@RequestMapping(value = "v1/examination-room",produces = {"application/prs.hal-forms+json", MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class RoomController {

    private final EventStoreDBClient client;
    private final ExaminationRoomCollector collector;

    @PostMapping
    @SneakyThrows
    public Mono<EntityModel<ExaminationRoom>> createRoom(@RequestBody CreateExaminationRoomRequest request){
        ExaminationRoomCreatedCommand command = new ExaminationRoomCreatedCommand();
        command.setId(UUID.randomUUID());
        command.setEventTimestamp(ZonedDateTime.now());
        command.setRoomNumber(request.getRoomNumber());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(), eventData)).then(getExaminationRoom(command.getId()));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<ExaminationRoom>>> getExaminationRoomResponse(){
        RoomController roomController = methodOn(RoomController.class);
         return getExaminationRooms().flatMap(
                e -> linkTo(roomController.getExaminationRoom(e.id()))
                        .withSelfRel()
                        .andAffordance(roomController.updateExaminationRoom(e.id(),null))
                        .andAffordance(roomController.getExaminationRoom(e.id()))
                        .toMono().map(selfLink -> EntityModel.of(e, selfLink))
        ).collectList().map(CollectionModel::of);
    }

    public Flux<ExaminationRoom> getExaminationRooms(){
        ExaminationRoomContext context = new ExaminationRoomContext();
        return collector.getCommands()
                .transform(context::acceptCommand)
                .flatMapIterable(Map::values);
    }


    @GetMapping("{roomId}")
    public Mono<EntityModel<ExaminationRoom>> getExaminationRoom(@PathVariable UUID roomId){
        RoomController roomController = methodOn(RoomController.class);
        Mono<Link> allLink = linkTo(roomController.getExaminationRoomResponse())
                .withRel("all").toMono();
        return getExaminationRooms()
                .filter(r->r.id().equals(roomId))
                .next()
                .switchIfEmpty(Mono.error(new ExaminationRoomNotFound(roomId)))
                .flatMap(e->
                        linkTo(roomController.getExaminationRoom(roomId))
                                .withSelfRel()
                                .andAffordance(roomController.updateExaminationRoom(roomId,null))
                                .toMono().map(link -> EntityModel.of(e,link))
                                .zipWith(allLink, RepresentationModel::add)

                );
    }

    @PutMapping("{roomId}")
    public Mono<EntityModel<ExaminationRoom>> updateExaminationRoom(@PathVariable UUID roomId, @RequestBody CreateExaminationRoomRequest examinationRoom){
        ExaminationRoomUpdateCommand command = new ExaminationRoomUpdateCommand();
        BeanUtils.copyProperties(examinationRoom,command);
        command.setId(roomId);
        command.setEventTimestamp(ZonedDateTime.now());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(), eventData)).flatMap((result)->getExaminationRoom(roomId));
    }

    @DeleteMapping("{roomId}")
    public Mono<EntityModel<ExaminationRoom>> updateExaminationRoom(@PathVariable UUID roomId) {
        ExaminationRoomDeletedCommand command = new ExaminationRoomDeletedCommand();
        command.setId(roomId);
        command.setEventTimestamp(ZonedDateTime.now());
        EventData eventData = EventUtility.buildEventData(command);
        return Mono.fromFuture(client.appendToStream(command.getStreamName(), eventData)).flatMap((result)->getExaminationRoom(roomId));
    }
}
