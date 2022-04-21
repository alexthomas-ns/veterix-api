package com.veterix.api.commands.appointment;

import com.veterix.api.commands.BaseCommand;
import com.veterix.api.model.Appointment;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
public sealed abstract class AppointmentCommand implements BaseCommand permits CreateAppointmentCommand {
    private UUID id;
    private ZonedDateTime eventTimestamp;
    @Override
    public String getStreamName() {
        return "appointments";
    }

    /**
     * This method with create a new instance of an appointment, the original value will not be mutated
     * @return a copy of the input appointment with the effect of the command applied
     */
    public abstract Optional<Appointment> applyCommand (Appointment examinationRoom);

}
