package org.example.account.service;

import org.example.account.domain.Account;
import org.example.account.type.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountServiceTest {
    @Autowired
    private AccountService accountService;
    
    @BeforeEach
    void init() {
        accountService.createAccount(1L, 10000L);
    }
    
    @Test
    @DisplayName("첫 번째 계좌 생성")
    void testGetAccount1() {
        // given
        Account account = accountService.getAccount(1L);
        
        // when
        String accountNumber = account.getAccountNumber();
        AccountStatus accountStatus = account.getAccountStatus();
        
        // then
        assertEquals("1000000000", accountNumber);
        assertEquals(AccountStatus.IN_USE, accountStatus);
    }
    
    @Test
    @DisplayName("두 번째 계좌 생성")
    void testGetAccount2() {
        // given
        Account account = accountService.getAccount(2L);
        
        // when
        String accountNumber = account.getAccountNumber();
        AccountStatus accountStatus = account.getAccountStatus();
        
        // then
        assertEquals("1000000001", accountNumber);
        assertEquals(AccountStatus.IN_USE, accountStatus);
    }
}