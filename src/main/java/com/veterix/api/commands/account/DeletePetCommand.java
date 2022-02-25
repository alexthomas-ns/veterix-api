package com.veterix.api.commands.account;

import com.veterix.api.model.Account;
import com.veterix.api.model.Pet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public final class DeletePetCommand extends AccountCommand {
    private UUID petId;

    @Override
    public Optional<Account> applyCommand(Account account) {
        List<Pet> pets = account.pets().stream()
                .filter(p -> !p.id().equals(petId))
                .toList();
        Account modifiedAccount = new Account(account.id(), account.name(), account.email(),
                account.phoneNumber(), account.address(), pets, account.paymentInformation());
        return Optional.of(modifiedAccount);
    }

    @Override
    public String getType() {
        return "delete-pet";
    }
}
