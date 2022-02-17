package com.veterix.api.collector;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ReadResult;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.veterix.api.commands.room.ExaminationRoomCommand;
import com.veterix.api.commands.room.ExaminationRoomCreatedCommand;
import com.veterix.api.commands.room.ExaminationRoomDeletedCommand;
import com.veterix.api.commands.room.ExaminationRoomUpdateCommand;
import com.veterix.api.util.EventUtility;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
public class ExaminationRoomCollector extends BaseCollector<ExaminationRoomCommand>{


    @Autowired
    public ExaminationRoomCollector(EventStoreDBClient client) {
        super(client);
    }

    @Override
    String getStreamName() {
        return new ExaminationRoomCreatedCommand().getStreamName(); //create an instance of any examination room command to get the stream name
    }

    @SneakyThrows
    ExaminationRoomCommand parseEvent(ResolvedEvent resolvedEvent) {
        RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
        String eventType = originalEvent.getEventType();
        return switch (eventType) {
            case "create-examination-room" -> EventUtility.parseEventData(originalEvent,ExaminationRoomCreatedCommand.class);
            case "update-examination-room" -> EventUtility.parseEventData(originalEvent, ExaminationRoomUpdateCommand.class);
            case "delete-examination-room" -> EventUtility.parseEventData(originalEvent, ExaminationRoomDeletedCommand.class);
            default -> throw new RuntimeException("Unknown event type:  " + eventType);
        };
    }
}
