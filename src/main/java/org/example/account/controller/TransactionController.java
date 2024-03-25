package org.example.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.account.dto.CancelBalance;
import org.example.account.dto.UseBalance;
import org.example.account.exception.AccountException;
import org.example.account.service.TransactionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TransactionController {
    private final TransactionService transactionService;
    
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request) {
        
        try {
            return UseBalance.Response.from(transactionService.useBalance(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getAmount()
            ));
        } catch (AccountException e) {
            log.error(e.getErrorMessage());
            
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(), request.getAmount());
            
            throw e;
        }
    }
    
    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request) {
        
        try {
            return CancelBalance.Response.from(transactionService.cancelBalance(
                    request.getTransactionId(),
                    request.getAccountNumber(),
                    request.getAmount()
            ));
        } catch (AccountException e) {
            log.error(e.getErrorMessage());
            
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(), request.getAmount());
            
            throw e;
        }
    }
}
