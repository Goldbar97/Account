package org.example.account.service;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private AccountUserRepository accountUserRepository;
    
    @Mock
    private AccountRepository accountRepository;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private Long CANCEL_AMOUNT = 200L;
    
    @Test
    @DisplayName("거래 성공")
    void successUseBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                                    .account(account)
                                    .transactionType(TransactionType.USE)
                                    .transactionResultType(
                                            TransactionResultType.S)
                                    .transactionId("transactionId")
                                    .transactedAt(LocalDateTime.now())
                                    .amount(1000L)
                                    .balanceSnapshot(9000L)
                                    .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(
                Transaction.class);
        
        // when
        TransactionDto transactionDto = transactionService.useBalance(
                1L, "1000000012", 200L);
        
        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapshot());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(
                TransactionResultType.S,
                transactionDto.getTransactionResultType()
        );
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
    }
    
    @Test
    @DisplayName("해당 유저 없음 - 거래 실패")
    void useBalance_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L)
        );
        
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("해당 계좌 없음 - 거래 실패")
    void useBalance_AccountNotFound() {
        // given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();
        
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L)
        );
        
        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("계좌 소유주 다름 - 거래 실패")
    void useBalance_UserUnmatched() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        AccountUser otherUser = AccountUser.builder()
                .id(13L)
                .name("Harry")
                .build();
        
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                                .accountUser(otherUser)
                                                .balance(0L)
                                                .accountNumber("10000000012")
                                                .build()));
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "0123456789", 1000L)
        );
        
        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("이미 해지된 계좌 - 거래 실패")
    void useBalance_AlreadyUnregistered() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                                .accountUser(user)
                                                .accountStatus(
                                                        AccountStatus.UNREGISTERED)
                                                .balance(0L)
                                                .accountNumber("10000000012")
                                                .build()));
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "10000000000", 1000L)
        );
        
        // then
        assertEquals(
                ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,
                exception.getErrorCode()
        );
    }
    
    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우")
    void exceedAmount_useBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(100L)
                .accountNumber("1000000012")
                .build();
        
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        
        // when
        // then
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.useBalance(1L, "10000000000", 1000L)
        );
        
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }
    
    @Test
    @DisplayName("실패한 트랜잭션 저장 성공")
    void saveFailedUseTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                                    .account(account)
                                    .transactionType(TransactionType.USE)
                                    .transactionResultType(
                                            TransactionResultType.S)
                                    .transactionId("transactionId")
                                    .transactedAt(LocalDateTime.now())
                                    .amount(1000L)
                                    .balanceSnapshot(9000L)
                                    .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(
                Transaction.class);
        
        // when
        transactionService.saveFailedUseTransaction("1000000000", 200L);
        
        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(
                TransactionResultType.F,
                captor.getValue().getTransactionResultType()
        );
    }
    
    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResultType(
                        TransactionResultType.S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .build();
        
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                                    .account(account)
                                    .transactionType(TransactionType.CANCEL)
                                    .transactionResultType(
                                            TransactionResultType.S)
                                    .transactionId("transactionIdForCancel")
                                    .transactedAt(LocalDateTime.now())
                                    .amount(CANCEL_AMOUNT)
                                    .balanceSnapshot(10000L)
                                    .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(
                Transaction.class);
        
        // when
        TransactionDto transactionDto = transactionService.cancelBalance(
                "transactionId", "1000000000", CANCEL_AMOUNT);
        
        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
        assertEquals(
                10000L + CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot());
        assertEquals(
                TransactionResultType.S,
                captor.getValue().getTransactionResultType()
        );
        assertEquals(
                TransactionType.CANCEL, captor.getValue().getTransactionType());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
    }
    
    @Test
    @DisplayName("해당 계좌 없음 - 거래 취소 실패")
    void cancelTransaction_AccountNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId", "1000000000", 1000L)
        );
        
        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("해당 거래 없음 - 거래 취소 실패")
    void cancelTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId", "1000000000", 1000L)
        );
        
        // then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
    
    @Test
    @DisplayName("거래 계좌 매칭 실패 - 거래 취소 실패")
    void cancelTransaction_TransactionAccountUnMatch() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        Account accountNotUse = Account.builder()
                .accountUser(user)
                .id(2L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000013")
                .build();
        
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResultType(
                        TransactionResultType.S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .build();
        
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(accountNotUse));
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId", "1000000000", CANCEL_AMOUNT)
        );
        
        // then
        assertEquals(
                ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH,
                exception.getErrorCode()
        );
    }
    
    @Test
    @DisplayName("거래 금액 매칭 실패 - 거래 취소 실패")
    void cancelTransaction_CancelMustFully() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResultType(
                        TransactionResultType.S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(CANCEL_AMOUNT + 1000L)
                .balanceSnapshot(10000L)
                .build();
        
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId", "1000000000", CANCEL_AMOUNT)
        );
        
        // then
        assertEquals(
                ErrorCode.CANCEL_MUST_FULLY,
                exception.getErrorCode()
        );
    }
    
    @Test
    @DisplayName("거래 기간 매칭 실패 - 거래 취소 실패")
    void cancelTransaction_TooOldOrder() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResultType(
                        TransactionResultType.S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .build();
        
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId", "1000000000", CANCEL_AMOUNT)
        );
        
        // then
        assertEquals(
                ErrorCode.TOO_OLD_ORDER_TO_CANCEL,
                exception.getErrorCode()
        );
    }
    
    @Test
    void successQueryTransaction() {
        // given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();
        
        Account account = Account.builder()
                .accountUser(user)
                .id(1L)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResultType(
                        TransactionResultType.S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .amount(CANCEL_AMOUNT)
                .balanceSnapshot(10000L)
                .build();
        
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        
        // when
        TransactionDto transactionDto = transactionService.queryTransaction(
                "trxId");
        
        // then
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(
                TransactionResultType.S,
                transactionDto.getTransactionResultType()
        );
        assertEquals(CANCEL_AMOUNT, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }
    
    @Test
    @DisplayName("해당 거래 없음 - 거래 조회 실패")
    void queryTransaction_TransactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        
        // when
        AccountException exception = assertThrows(
                AccountException.class,
                () -> transactionService.queryTransaction("transactionId")
        );
        
        // then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}