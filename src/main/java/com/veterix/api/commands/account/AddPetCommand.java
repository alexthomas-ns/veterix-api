package com.veterix.api.commands.account;

import com.veterix.api.model.Account;
import com.veterix.api.model.Pet;
import com.veterix.api.model.PetSpecies;
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
    private PetSpecies species;
    private UUID petId;
    private String breed;
    private int age;
    @Override
    public Optional<Account> applyCommand(Account account) {
        List<Pet> pets = new ArrayList<>();
        if(account.pets()!=null){
            pets.addAll(account.pets());
        }
        Pet pet = new Pet(petId,name,species,breed,age,this.getEventTimestamp());
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
