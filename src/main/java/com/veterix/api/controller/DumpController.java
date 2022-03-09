package com.veterix.api.controller;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.RecordedEvent;
import com.eventstore.dbclient.ResolvedEvent;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veterix.api.commands.BaseCommand;
import com.veterix.api.commands.account.CreateAccountCommand;
import com.veterix.api.commands.room.ExaminationRoomCreatedCommand;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequestMapping("v1/dump")
@AllArgsConstructor
@RestController
public class DumpController {
    private static final List<BaseCommand> BASE_COMMANDS = List.of(
            new CreateAccountCommand(),
            new ExaminationRoomCreatedCommand()
    );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EventStoreDBClient client;

    record Event(@JsonRawValue String eventData, String eventType) {
    }

    @SneakyThrows
    ZipOutputStream createOutputStream(String fileName){
        Path tempFile = Files.createFile(Path.of(fileName));
        return new ZipOutputStream(new FileOutputStream(tempFile.toFile()));
    }

    @GetMapping
    public Mono<Resource> dump() {
        String fileName = "dump"+UUID.randomUUID() +".zip";
        Flux<ZipOutputStream> zipOutputStream = Mono.fromSupplier(() -> createOutputStream(fileName)).cache().repeat();
        return Flux.fromIterable(BASE_COMMANDS)
                .map(BaseCommand::getStreamName)
                .zipWith(zipOutputStream)
                .flatMapSequential((n) -> writeStreamToZip(n.getT1(), n.getT2()),1)
                .then(zipOutputStream.next().map(z->finishFile(fileName,z)));
    }

    @SneakyThrows
    Resource finishFile(String fileName,ZipOutputStream zipOutputStream){
        zipOutputStream.close();
        return new FileSystemResource(fileName);
    }


    @SneakyThrows
    void writeNextEntry(ZipEntry zipEntry, ZipOutputStream outputStream) {
        outputStream.putNextEntry(zipEntry);
    }


    @SneakyThrows
    Mono<Void> writeStreamToZip(String streamName, ZipOutputStream outputStream) {
        ZipEntry zipEntry = new ZipEntry(streamName + ".json");
        Flux<DataBuffer> eventsData = Mono.fromRunnable(() -> writeNextEntry(zipEntry, outputStream))
                .thenMany(client.readStreamReactive(streamName))
                .map(this::resolveEventBytes);
        return DataBufferUtils.write(eventsData, outputStream).then();

    }

    @SneakyThrows
    DataBuffer resolveEventBytes(ResolvedEvent resolvedEvent) {
        RecordedEvent originalEvent = resolvedEvent.getOriginalEvent();
        String eventData = new String(originalEvent.getEventData());
        Event event = new Event(eventData, originalEvent.getEventType());
        byte[] bytes = (OBJECT_MAPPER.writeValueAsString(event) + "\n").getBytes();
        return DefaultDataBufferFactory.sharedInstance.wrap(bytes);
    }
}
