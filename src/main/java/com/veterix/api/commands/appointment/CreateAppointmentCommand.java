package com.veterix.api.commands.appointment;

import com.veterix.api.model.Appointment;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public final class CreateAppointmentCommand extends AppointmentCommand{

    private UUID account;
    private UUID pet;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;


    @Override
    public Optional<Appointment> applyCommand(Appointment appointment) {
        return Optional.of(new Appointment(getId(), account, pet, startTime, endTime));
    }

    @Override
    public String getType() {
        return "create-appointment";
    }
}
