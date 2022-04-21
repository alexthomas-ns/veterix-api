package com.veterix.api.commands.appointment;

import com.veterix.api.model.Appointment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class AppointmentContext {
    private final Map<UUID, Appointment> appointments = new ConcurrentHashMap<>();
    private final Appointment DEFAULT_APPOINTMENT = new Appointment(null,null,null,null,null);

    public Mono<Map<UUID,Appointment>> acceptCommand(Flux<AppointmentCommand> commandFlux){
        return commandFlux.doOnNext(this::acceptCommand).then(Mono.just(Collections.unmodifiableMap(appointments)));
    }

    void acceptCommand(AppointmentCommand command){
       Appointment appointment = appointments.getOrDefault(command.getId(), DEFAULT_APPOINTMENT);
        Optional<Appointment> commandResult = command.applyCommand(appointment);
        commandResult.ifPresentOrElse(e->appointments.put(command.getId(),e),()->appointments.remove(command.getId()));
    }
}
