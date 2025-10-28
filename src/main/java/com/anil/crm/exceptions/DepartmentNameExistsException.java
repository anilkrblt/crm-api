package com.anil.crm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.CONFLICT)
public class DepartmentNameExistsException extends RuntimeException {

    public DepartmentNameExistsException(String message) {
        super(message);
    }
}