package com.veterix.api.commands.account;

import com.veterix.api.model.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class AccountContext {
    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();
    private final Account DEFAULT_ACCOUNT = new Account(null,null,null,null,null,null,null);

    public Mono<Map<UUID,Account>> acceptCommand(Flux<AccountCommand> commandFlux){
        return commandFlux.doOnNext(this::acceptCommand).then(Mono.just(Collections.unmodifiableMap(accounts)));
    }

    void acceptCommand(AccountCommand command){
       Account account = accounts.getOrDefault(command.getId(),DEFAULT_ACCOUNT);
        Optional<Account> commandResult = command.applyCommand(account);
        commandResult.ifPresentOrElse(e->accounts.put(command.getId(),e),()->accounts.remove(command.getId()));
    }
}
