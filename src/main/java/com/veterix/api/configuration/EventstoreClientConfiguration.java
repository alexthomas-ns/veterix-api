package com.veterix.api.configuration;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventstoreClientConfiguration {

    @Bean
    public EventStoreDBClientSettings getSettings(){
        return EventStoreDBConnectionString.parseOrThrow("esdb://docker.local:2113?tls=false");
    }

    @Bean
    public EventStoreDBClient getClient(EventStoreDBClientSettings settings){
        return EventStoreDBClient.create(settings);
    }
}
