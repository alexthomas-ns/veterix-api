package com.veterix.api.commands.account;

import com.veterix.api.model.Account;
import com.veterix.api.model.Pet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public final class AddPetCommand extends AccountCommand{
    private String name;
    private String petType;
    private UUID petId;
    private String breed;
    @Override
    public Optional<Account> applyCommand(Account account) {
        List<Pet> pets = new ArrayList<>();
        if(account.pets()!=null){
            pets.addAll(account.pets());
        }
        Pet pet = new Pet(petId,name,petType,breed,this.getEventTimestamp());
        pets.add(pet);
        Account finalAccount = new Account(
                account.id(),
                account.name(),
                account.email(),
                account.phoneNumber(),
                account.address(),
                pets,
                account.paymentInformation()
        );
        return Optional.of(finalAccount);
    }

    @Override
    public String getType() {
        return "add-pet";
    }
}
