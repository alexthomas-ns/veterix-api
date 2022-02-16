package com.veterix.api.commands.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veterix.api.model.ExaminationRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public final class ExaminationRoomCreatedCommand extends ExaminationRoomCommand {
    private String roomNumber;

    @Override
    public Optional<ExaminationRoom> applyCommand(ExaminationRoom ignoredInput) {
        ExaminationRoom examinationRoom = new ExaminationRoom(
                this.getId(),
               roomNumber,
               this.getEventTimestamp(),
               this.getEventTimestamp()
        );
        return Optional.of(examinationRoom);
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "create-examination-room";
    }


}
