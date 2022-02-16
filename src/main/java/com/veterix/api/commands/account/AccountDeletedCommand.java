package com.veterix.api.commands.account;

import com.veterix.api.model.Account;

import java.util.Optional;

public final class AccountDeletedCommand extends AccountCommand{
    @Override
    public Optional<Account> applyCommand(Account account) {
        return Optional.empty();
    }

    @Override
    public String getType() {
        return "delete-account";
    }
}
