package com.veterix.api.util;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventDataBuilder;
import com.eventstore.dbclient.RecordedEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.veterix.api.commands.BaseCommand;
import lombok.SneakyThrows;

public class EventUtility {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper() ;
        OBJECT_MAPPER.findAndRegisterModules();
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

    @SneakyThrows
    public static EventData buildEventData(BaseCommand command){
        byte[] eventString = OBJECT_MAPPER.writeValueAsBytes(command);
        return EventDataBuilder.json(command.getType(), eventString).build();
    }

    @SneakyThrows
    public static <T> T parseEventData(RecordedEvent eventData, Class<T> clazz){
       return OBJECT_MAPPER.readValue(eventData.getEventData(),clazz);
    }
}
