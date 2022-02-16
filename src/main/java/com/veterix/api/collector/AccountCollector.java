package com.veterix.api.collector;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.veterix.api.commands.account.AccountCommand;
import com.veterix.api.commands.account.AccountDeletedCommand;
import com.veterix.api.commands.account.AccountUpdateCommand;
import com.veterix.api.commands.account.AddPetCommand;
import com.veterix.api.commands.account.CreateAccountCommand;
import com.veterix.api.commands.room.ExaminationRoomCommand;
import com.veterix.api.commands.room.ExaminationRoomCreatedCommand;
import com.veterix.api.commands.room.ExaminationRoomDeletedCommand;
import com.veterix.api.commands.room.ExaminationRoomUpdateCommand;
import com.veterix.api.model.Account;
import com.veterix.api.util.EventUtility;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@AllArgsConstructor
@Component
public class AccountCollector {

    private final EventStoreDBClient client;

    public Flux<AccountCommand> getAccountCommands() {
        String streamName = new CreateAccountCommand().getStreamName(); //create an instance of any account command to get the stream name
        return Flux.from(client.readStreamReactive(streamName)).map(this::parseEvent);
    }

    @SneakyThrows
    AccountCommand parseEvent(ResolvedEvent resolvedEvent) {
        RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
        String eventType = originalEvent.getEventType();
        return switch (eventType) {
            case "create-account"-> EventUtility.parseEventData(originalEvent,CreateAccountCommand.class);
            case "add-pet" -> EventUtility.parseEventData(originalEvent, AddPetCommand.class);
            case "update-account"-> EventUtility.parseEventData(originalEvent, AccountUpdateCommand.class);
            case "delete-account" -> EventUtility.parseEventData(originalEvent, AccountDeletedCommand.class);
            default -> throw new RuntimeException("Unknown event type:  " + eventType);
        };
    }
}
