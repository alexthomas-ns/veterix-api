package com.veterix.api.commands.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veterix.api.commands.BaseCommand;
import com.veterix.api.model.Account;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
public sealed abstract class AccountCommand implements BaseCommand permits AccountDeletedCommand, AccountUpdateCommand, AddPetCommand, CreateAccountCommand {

    private UUID id;
    private ZonedDateTime eventTimestamp;

    @JsonIgnore
    public String getStreamName(){
        return "accounts";
    }

    public abstract Optional<Account> applyCommand(Account account);

}
