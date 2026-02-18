package com.ride.mate.core;

import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.resources.SendVerificationCodeRequest;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.ValidateResource;
import com.ride.mate.resources.VerifyCodeRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.Field;

/**
 * Base Response Entity Exception Handler
 * Global exception handler for validation errors
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A         Tishan          Initial Development
 */
@RestControllerAdvice
public class BaseResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final Environment environment;

    public BaseResponseEntityExceptionHandler(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        try {
            String className = ex.getBindingResult().getObjectName();
            if ("sendVerificationCodeRequest".equals(className)) {
                SendVerificationCodeRequest resource = new SendVerificationCodeRequest();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                    Field field = resource.getClass().getDeclaredField(error.getField());
                    field.setAccessible(true);
                    field.set(resource, error.getDefaultMessage());
                }
                return new ResponseEntity<>(resource, HttpStatus.BAD_REQUEST);
            } else if ("verifyCodeRequest".equals(className)) {
                VerifyCodeRequest resource = new VerifyCodeRequest();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                    Field field = resource.getClass().getDeclaredField(error.getField());
                    field.setAccessible(true);
                    field.set(resource, error.getDefaultMessage());
                }
                return new ResponseEntity<>(resource, HttpStatus.UNPROCESSABLE_ENTITY);
            } else {
                SuccessAndErrorDetailsResource errorDetails = new SuccessAndErrorDetailsResource();
                StringBuilder errorMessage = new StringBuilder();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                    errorMessage.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
                }
                errorDetails.setMessages("Validation Failed");
                errorDetails.setDetails(errorMessage.toString());
                return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        } catch (Exception e) {
            SuccessAndErrorDetailsResource errorDetails = new SuccessAndErrorDetailsResource();
            errorDetails.setMessages("ERROR");
            errorDetails.setDetails(e.getMessage());
            return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler({ValidateRecordException.class})
    public ResponseEntity<Object> validateRecordException(ValidateRecordException ex, WebRequest request) {
        try {
            ValidateResource typeValidation = new ValidateResource();
            Class validationDetailClass = typeValidation.getClass();
            Field sField = validationDetailClass.getDeclaredField(ex.getField());
            sField.setAccessible(true);
            sField.set(typeValidation, ex.getMessage());
            return new ResponseEntity<>(typeValidation, HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (Exception e) {
            SuccessAndErrorDetailsResource errorDetails = new SuccessAndErrorDetailsResource();
            errorDetails.setMessages("ERROR");
            errorDetails.setDetails(e.getMessage());
            return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
