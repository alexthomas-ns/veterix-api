package com.veterix.api.commands.room;

import com.veterix.api.model.ExaminationRoom;

import java.util.Optional;

public final class ExaminationRoomDeletedCommand extends ExaminationRoomCommand{
    @Override
    public Optional<ExaminationRoom> applyCommand(ExaminationRoom examinationRoom) {
        return Optional.empty();
    }

    @Override
    public String getType() {
        return "delete-examination-room";
    }
}
