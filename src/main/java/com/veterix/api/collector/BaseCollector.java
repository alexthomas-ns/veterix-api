package com.veterix.api.collector;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.ResolvedEvent;
import com.eventstore.dbclient.StreamNotFoundException;
import com.veterix.api.commands.BaseCommand;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@AllArgsConstructor
@Slf4j
public abstract class BaseCollector<T extends BaseCommand> {
    private final EventStoreDBClient client;

    abstract String getStreamName();

    public Flux<T> getCommands(){
        String streamName = getStreamName();
        return Flux.from(client.readStreamReactive(streamName)).map(this::parseEvent).onErrorResume(StreamNotFoundException.class,(throwable)->{
            log.warn("{} stream not found in eventstore",streamName);
            return Flux.empty();
        });
    }

    abstract T parseEvent(ResolvedEvent resolvedEvent);
}
