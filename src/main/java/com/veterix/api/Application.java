package com.veterix.api;

import com.eventstore.dbclient.EventData;
import com.eventstore.dbclient.EventStoreDBClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class, args);

        EventStoreDBClient dbClient = run.getBean(EventStoreDBClient.class);
        SomeData myContent = new SomeData("my content");
        EventData eventData = EventData.builderAsJson("dummy-event", myContent).build();
        Mono.fromFuture(dbClient.appendToStream("test-stream",eventData)).block();
    }

    public record SomeData(String content){}

}
