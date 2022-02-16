package com.veterix.api.commands.room;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veterix.api.commands.BaseCommand;
import com.veterix.api.model.ExaminationRoom;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
public abstract sealed class ExaminationRoomCommand implements BaseCommand permits ExaminationRoomCreatedCommand, ExaminationRoomDeletedCommand, ExaminationRoomUpdateCommand {
   private UUID id;
   private ZonedDateTime eventTimestamp;

   @JsonIgnore
   public String getStreamName(){
      return "examination-rooms";
   }

   /**
    * This method with create a new instance of an examination room, the original value will not be mutated
    * @return a copy of the input examination room with the effect of the command applied
    */
   public abstract Optional<ExaminationRoom> applyCommand (ExaminationRoom examinationRoom);

}
