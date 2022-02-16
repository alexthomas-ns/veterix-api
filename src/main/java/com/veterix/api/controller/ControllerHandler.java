package com.veterix.api.controller;

import com.veterix.api.exception.ExceptionWithDiscoverability;
import com.veterix.api.exception.room.AccountNotFound;
import com.veterix.api.exception.room.ExaminationRoomNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ControllerHandler {

    @ExceptionHandler({ExaminationRoomNotFound.class, AccountNotFound.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public  Object process404(HttpServletRequest request,Exception ex){
        if(ex instanceof ExceptionWithDiscoverability exceptionWithDiscoverability){
            return exceptionWithDiscoverability.toEntityModel();
        }
       return ex;
    }
}
