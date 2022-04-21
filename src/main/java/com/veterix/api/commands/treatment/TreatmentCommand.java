package com.veterix.api.commands.treatment;

import com.veterix.api.commands.BaseCommand;

public abstract class TreatmentCommand implements BaseCommand {
    @Override
    public String getStreamName() {
        return "treatments";
    }
}
