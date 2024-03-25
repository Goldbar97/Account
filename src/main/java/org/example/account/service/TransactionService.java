package org.example.account.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.account.domain.Account;
import org.example.account.domain.AccountUser;
import org.example.account.domain.Transaction;
import org.example.account.dto.TransactionDto;
import org.example.account.exception.AccountException;
import org.example.account.repository.AccountRepository;
import org.example.account.repository.AccountUserRepository;
import org.example.account.repository.TransactionRepository;
import org.example.account.type.AccountStatus;
import org.example.account.type.ErrorCode;
import org.example.account.type.TransactionResultType;
import org.example.account.type.TransactionType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;
    
    @Transactional
    public TransactionDto useBalance(
            Long userId, String accountNumber,
            Long amount) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(
                        () -> new AccountException(ErrorCode.USER_NOT_FOUND));
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(
                        ErrorCode.ACCOUNT_NOT_FOUND));
        
        validateUseBalance(accountUser, account, amount);
        
        account.useBalance(amount);
        
        return TransactionDto.fromEntity(
                saveAndGetTransaction(
                        TransactionResultType.S, account, amount));
    }
    
    private void validateUseBalance(
            AccountUser accountUser, Account account,
            Long amount) {
        if (!Objects.equals(
                accountUser.getId(),
                account.getAccountUser().getId()
        )) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        
        if (account.getBalance() < amount) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
    }
    
    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(
                        ErrorCode.ACCOUNT_NOT_FOUND));
        
        saveAndGetTransaction(TransactionResultType.F, account, amount);
    }
    
    private Transaction saveAndGetTransaction(
            TransactionResultType transactionResultType, Account account,
            Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(
                                TransactionType.USE)
                        .transactionResultType(
                                transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(
                                account.getBalance())
                        .transactionId(
                                UUID.randomUUID()
                                        .toString()
                                        .replace(
                                                "-",
                                                ""
                                        ))
                        .transactedAt(
                                LocalDateTime.now())
                        .build());
    }
}

