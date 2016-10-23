package com.eli.fozo.validation;

import com.eli.fozo.controller.WebAPIController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by Elias on 10/23/2016.
 */

@ControllerAdvice(assignableTypes= WebAPIController.class)
public class WebAPIControllerAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        ValidationError error = ValidationErrorBuilder.fromBindingErrors(exception.getBindingResult());
        return super.handleExceptionInternal(exception, error, headers, status, request);
    }

}
