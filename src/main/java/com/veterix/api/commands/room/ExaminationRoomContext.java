package com.veterix.api.commands.room;

import com.veterix.api.model.ExaminationRoom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class ExaminationRoomContext {
    private final Map<UUID, ExaminationRoom> examinationRooms = new ConcurrentHashMap<>();
    private final ExaminationRoom DEFAULT_EXAMINATION_ROOM = new ExaminationRoom(null,null,null,null);

    public Mono<Map<UUID,ExaminationRoom>> acceptCommand(Flux<ExaminationRoomCommand> commandFlux){
        return commandFlux.doOnNext(this::acceptCommand).then(Mono.just(Collections.unmodifiableMap(examinationRooms)));
    }

    void acceptCommand(ExaminationRoomCommand command){
       ExaminationRoom examinationRoom = examinationRooms.getOrDefault(command.getId(),DEFAULT_EXAMINATION_ROOM);
        Optional<ExaminationRoom> commandResult = command.applyCommand(examinationRoom);
        commandResult.ifPresentOrElse(e->examinationRooms.put(command.getId(),e),()->examinationRooms.remove(command.getId()));
    }
}
