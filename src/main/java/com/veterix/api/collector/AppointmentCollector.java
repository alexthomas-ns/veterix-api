package com.veterix.api.collector;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.veterix.api.commands.account.AccountCommand;
import com.veterix.api.commands.account.AccountDeletedCommand;
import com.veterix.api.commands.account.AccountUpdateCommand;
import com.veterix.api.commands.account.AddPetCommand;
import com.veterix.api.commands.account.CreateAccountCommand;
import com.veterix.api.commands.account.DeletePetCommand;
import com.veterix.api.commands.appointment.AppointmentCommand;
import com.veterix.api.commands.appointment.CreateAppointmentCommand;
import com.veterix.api.util.EventUtility;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppointmentCollector extends BaseCollector<AppointmentCommand>{

    @Autowired
    public AppointmentCollector(EventStoreDBClient client) {
        super(client);
    }

    @Override
    String getStreamName() {
        return new CreateAppointmentCommand().getStreamName(); //create an instance of any account command to get the stream name
    }

    @SneakyThrows
    AppointmentCommand parseEvent(ResolvedEvent resolvedEvent) {
        RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
        String eventType = originalEvent.getEventType();
        return switch (eventType) {
            case "create-appointment"-> EventUtility.parseEventData(originalEvent,CreateAppointmentCommand.class);
            default -> throw new RuntimeException("Unknown event type:  " + eventType);
        };
    }
}
