package org.example.account.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.example.account.dto.UseBalance;
import org.example.account.exception.AccountException;
import org.example.account.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LockAopAspectTest {
    @Mock
    private LockService lockService;
    
    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;
    
    @InjectMocks
    private LockAopAspect lockAopAspect;
    
    @Test
    void lockAndUnlock() throws Throwable {
        // given
        ArgumentCaptor<String> lockCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockCaptor =
                ArgumentCaptor.forClass(String.class);
        UseBalance.Request request = new UseBalance.Request(123L, "1234",
                                                            10000L
        );
        
        // when
        lockAopAspect.aroundMethod(proceedingJoinPoint, request);
        
        
        // then
        verify(lockService, times(1)).lock(lockCaptor.capture());
        verify(lockService, times(1)).unlock(unLockCaptor.capture());
        assertEquals("1234", lockCaptor.getValue());
        assertEquals("1234", unLockCaptor.getValue());
        
    }
    
    @Test
    void lockAndUnlock_evenIfThrow() throws Throwable {
        // given
        ArgumentCaptor<String> lockCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> unLockCaptor =
                ArgumentCaptor.forClass(String.class);
        UseBalance.Request request = new UseBalance.Request(123L, "54321",
                                                            10000L
        );
        given(proceedingJoinPoint.proceed())
                .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
        
        // when
        assertThrows(
                AccountException.class,
                () -> lockAopAspect.aroundMethod(
                        proceedingJoinPoint,
                        request
                )
        );
        
        // then
        verify(lockService, times(1)).lock(lockCaptor.capture());
        verify(lockService, times(1)).unlock(unLockCaptor.capture());
        assertEquals("54321", lockCaptor.getValue());
        assertEquals("54321", unLockCaptor.getValue());
    }
}