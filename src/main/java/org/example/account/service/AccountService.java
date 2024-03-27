package org.example.account.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.account.domain.Account;
import org.example.account.domain.AccountUser;
import org.example.account.dto.AccountDto;
import org.example.account.exception.AccountException;
import org.example.account.repository.AccountRepository;
import org.example.account.repository.AccountUserRepository;
import org.example.account.type.AccountStatus;
import org.example.account.type.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.example.account.type.AccountStatus.IN_USE;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;
    
    /**
     * 사용자가 있는지 조회
     * 계좌의 번호를 생성
     * 계좌를 저장하고, 그 정보를 넘긴다.
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = getAccountUser(userId);
        
        validateCreateAccount(accountUser);
        
        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(
                        account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");
        
        return AccountDto.fromEntity(accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));
    }
    
    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new AccountException(
                                ErrorCode.ACCOUNT_NOT_FOUND));
        
        validateDeleteAccount(accountUser, account);
        
        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());
        
        return AccountDto.fromEntity(account);
    }
    
    private AccountUser getAccountUser(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(
                        () -> new AccountException(ErrorCode.USER_NOT_FOUND));
        return accountUser;
    }
    
    @Transactional
    public Account getAccount(Long id) {
        return accountRepository.findById(id).get();
    }
    
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser user = getAccountUser(userId);
        
        List<Account> accounts = accountRepository.findByAccountUser(user);
        
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) == 10) {
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }
    
    private void validateDeleteAccount(
            AccountUser accountUser, Account account) {
        if (!Objects.equals(
                accountUser.getId(),
                account.getAccountUser().getId()
        )) {
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        
        if (account.getBalance() > 0) {
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }
}
