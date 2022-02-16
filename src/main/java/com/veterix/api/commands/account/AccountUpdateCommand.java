package com.veterix.api.commands.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.veterix.api.commands.room.ExaminationRoomCommand;
import com.veterix.api.model.Account;
import com.veterix.api.model.ExaminationRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public final class AccountUpdateCommand extends AccountCommand {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;

    @Override
    public Optional<Account> applyCommand(Account originalValue) {
        return Optional.of(new Account(this.getId(),name,email,phoneNumber,address,originalValue.pets()
                ,originalValue.paymentInformation()));
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "update-account";
    }


}
