package com.veterix.api.commands.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veterix.api.model.ExaminationRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public final class ExaminationRoomUpdateCommand extends ExaminationRoomCommand {
    private String roomNumber;

    @Override
    public Optional<ExaminationRoom> applyCommand(ExaminationRoom originalValue) {
        ExaminationRoom examinationRoom = new ExaminationRoom(
                originalValue.id(),
                roomNumber,
                originalValue.createdDateTime(),
                this.getEventTimestamp()
        );
        return Optional.of(examinationRoom);
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "update-examination-room";
    }


}
