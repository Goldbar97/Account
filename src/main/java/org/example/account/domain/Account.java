package org.example.account.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.account.exception.AccountException;
import org.example.account.type.AccountStatus;
import org.example.account.type.ErrorCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Setter
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;
    
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;
    
    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }
    
    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance += amount;
    }
}
