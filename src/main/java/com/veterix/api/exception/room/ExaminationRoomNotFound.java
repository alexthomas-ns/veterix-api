package com.veterix.api.exception.room;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.veterix.api.controller.RoomController;
import com.veterix.api.exception.ExceptionWithDiscoverability;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@JsonIncludeProperties("message")
public class ExaminationRoomNotFound extends ExceptionWithDiscoverability {
    public ExaminationRoomNotFound(UUID examinationRoomId){
        super("Examination Room "+examinationRoomId+" not found");
    }

    @Override
    public EntityModel<Exception> toEntityModel() {
        RoomController roomController = methodOn(RoomController.class);
        Link link = linkTo(roomController.getExaminationRooms()).withRel("all");
        return EntityModel.of(this, link);
    }
}
