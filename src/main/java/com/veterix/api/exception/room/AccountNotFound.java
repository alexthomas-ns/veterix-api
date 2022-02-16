package com.veterix.api.exception.room;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.veterix.api.controller.AccountController;
import com.veterix.api.controller.RoomController;
import com.veterix.api.exception.ExceptionWithDiscoverability;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@JsonIncludeProperties("message")
public class AccountNotFound extends ExceptionWithDiscoverability {
    public AccountNotFound(UUID accountId){
        super("Account "+accountId+" not found");
    }

    @Override
    public EntityModel<Exception> toEntityModel() {
        AccountController accountController = methodOn(AccountController.class);
        Link link = linkTo(accountController.getAccounts()).withRel("all");
        return EntityModel.of(this, link);
    }
}
