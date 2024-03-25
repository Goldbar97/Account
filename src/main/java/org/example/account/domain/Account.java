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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;
    
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public void useBalance(Long amount) {
        if (amount > balance) {
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance -= amount;
    }
}
