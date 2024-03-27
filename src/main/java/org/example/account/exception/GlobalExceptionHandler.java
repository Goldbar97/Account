package org.example.account.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.account.dto.ErrorResponse;
import org.example.account.type.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountException.class)
    public ErrorResponse handleAccountException(AccountException e) {
        log.error("{} is occurred.", e.getErrorCode());
        
        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(
            DataIntegrityViolationException e) {
        log.error("{} is occurred", e.getMessage());
        
        return new ErrorResponse(ErrorCode.INVALID_REQUEST, e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {
        log.error("{} is occurred", e.getMessage());
        
        return new ErrorResponse(ErrorCode.INVALID_REQUEST, e.getMessage());
    }
}
