package com.veterix.api.controller;

import com.eventstore.dbclient.EventStoreDBClient;
import com.veterix.api.collector.AppointmentCollector;
import com.veterix.api.collector.ExaminationRoomCollector;
import com.veterix.api.commands.appointment.AppointmentContext;
import com.veterix.api.commands.appointment.CreateAppointmentCommand;
import com.veterix.api.commands.room.ExaminationRoomContext;
import com.veterix.api.model.Appointment;
import com.veterix.api.model.request.CreateAppointmentRequest;
import com.veterix.api.util.EventUtility;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("v1/appointment")
@AllArgsConstructor
@Slf4j
public class AppointmentController {

    private static final LocalTime OPEN_TIME = LocalTime.of(9,0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(17,0);
    private static final int APPOINTMENT_DURATION_INCREMENTS = 30; //in minutes

    private final AppointmentCollector appointmentCollector;
    private final ExaminationRoomCollector examinationRoomCollector;
    private final EventStoreDBClient client;

    record AvailabilityWindow(ZonedDateTime startTime,ZonedDateTime endTime){};

    @GetMapping("availability")
    public Flux<AvailabilityWindow> getAvailabilityWindows(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startRange,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endRange){
        ExaminationRoomContext examinationRoomContext = new ExaminationRoomContext();
        Mono<Integer> roomCount = examinationRoomCollector.getCommands().transform(examinationRoomContext::acceptCommand)
                .map(Map::size).next();
        AppointmentContext appointmentContext = new AppointmentContext();
        Mono<Collection<Appointment>> currentAppointments = appointmentCollector.getCommands().transform(appointmentContext::acceptCommand).map(Map::values).next();
        return Mono.zip(roomCount,currentAppointments).flatMapIterable(t->resolveWindows(startRange,endRange,t.getT1(),t.getT2()));
    }

    List<AvailabilityWindow> resolveWindows(ZonedDateTime startRange,ZonedDateTime endRange,int roomCount,Collection<Appointment> currentAppointments){
        Map<ZonedDateTime, Long> takenWindows = resolveTakenWindows(currentAppointments);
        List<AvailabilityWindow> availableWindows = new ArrayList<>();
        new WindowIterator(startRange, endRange).forEachRemaining(window->{
           if(takenWindows.getOrDefault(window.startTime,0L)<roomCount) {
               availableWindows.add(window);
           }
        });
        return availableWindows;

    }

    Map<ZonedDateTime,Long> resolveTakenWindows(Collection<Appointment> appointments) {
       return appointments.stream()
               .collect(Collectors.groupingBy(x -> x.startTime().withZoneSameInstant(ZoneId.systemDefault()), Collectors.counting()));
    }

    private static class WindowIterator implements Iterator<AvailabilityWindow> {
        private ZonedDateTime cursor;
        private final ZonedDateTime endRange;

        private WindowIterator(ZonedDateTime startRange, ZonedDateTime endRange) {
            cursor = startRange.minusMinutes(startRange.getMinute()%APPOINTMENT_DURATION_INCREMENTS)
                    .withSecond(0).withNano(0)
                    .withZoneSameInstant(ZoneId.systemDefault());
            this.endRange = endRange.plusMinutes(APPOINTMENT_DURATION_INCREMENTS-endRange.getMinute()%APPOINTMENT_DURATION_INCREMENTS)
                    .withSecond(0).withNano(0)
                    .withZoneSameInstant(ZoneId.systemDefault());
        }

        @Override
        public boolean hasNext() {
            return endRange.isAfter(cursor);
        }

        @Override
        public AvailabilityWindow next() {

            if(cursor.toLocalTime().isBefore(OPEN_TIME)){
                cursor = ZonedDateTime.of(cursor.toLocalDate(),OPEN_TIME, ZoneId.systemDefault());
            } else if(cursor.toLocalTime().plusMinutes(APPOINTMENT_DURATION_INCREMENTS).isAfter(CLOSE_TIME)){
                cursor = ZonedDateTime.of(cursor.toLocalDate().plusDays(1),OPEN_TIME, ZoneId.systemDefault());
            }
            AvailabilityWindow window = new AvailabilityWindow(cursor,cursor.plusMinutes(APPOINTMENT_DURATION_INCREMENTS));
            cursor = cursor.plusMinutes(APPOINTMENT_DURATION_INCREMENTS);
            return window;

        }
    }

    @GetMapping
    public Mono<Collection<Appointment>> getAppointments(){
        AppointmentContext appointmentContext = new AppointmentContext();
        return appointmentCollector.getCommands().transform(appointmentContext::acceptCommand).map(Map::values).next();
    }

    @PostMapping
    public Mono<ResponseEntity<?>> scheduleAppointment(@RequestBody CreateAppointmentRequest request){
        ExaminationRoomContext examinationRoomContext = new ExaminationRoomContext();
        Mono<Integer> roomCount = examinationRoomCollector.getCommands().transform(examinationRoomContext::acceptCommand)
                .map(Map::size).next();
        AppointmentContext appointmentContext = new AppointmentContext();
        Mono<Boolean> isAvailable = appointmentCollector.getCommands().transform(appointmentContext::acceptCommand).map(Map::values).next()
                .map(this::resolveTakenWindows)
                .map(x -> x.getOrDefault(request.startTime().withZoneSameInstant(ZoneId.systemDefault()), 0L))
                .zipWith(roomCount)
                .map(t -> t.getT1() < t.getT2());
        return isAvailable.flatMap(i->{
            if(i){
                CreateAppointmentCommand createAppointmentCommand = new CreateAppointmentCommand();
                createAppointmentCommand.setAccount(request.account());
                createAppointmentCommand.setPet(request.pet());
                createAppointmentCommand.setId(UUID.randomUUID());
                createAppointmentCommand.setStartTime(request.startTime());
                createAppointmentCommand.setEndTime(request.endTime());
                createAppointmentCommand.setEventTimestamp(ZonedDateTime.now());
                return Mono.fromFuture(client.appendToStream(createAppointmentCommand.getStreamName(), EventUtility.buildEventData(createAppointmentCommand)))
                        .thenReturn(ResponseEntity.accepted().build());
            } else{
                return Mono.just(ResponseEntity.badRequest().body("There are no available appointments at that time"));
            }
        });

    }


}
