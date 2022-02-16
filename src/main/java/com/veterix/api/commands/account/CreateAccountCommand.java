package com.veterix.api.commands.account;

import com.veterix.api.model.Account;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public final class CreateAccountCommand extends AccountCommand{

    private String name;
    private String email;
    private String phoneNumber;
    private String address;

    @Override
    public Optional<Account> applyCommand(Account account) {
        return Optional.of(new Account(this.getId(),name,email,phoneNumber,address,null
        ,null));
    }

    @Override
    public String getType() {
        return "create-account";
    }
}
