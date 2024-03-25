package org.example.account.dto;

import lombok.*;
import org.example.account.domain.Account;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AccountDto {
    private Long userId;
    private String accountNumber;
    private Long balance;
    
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;
    
    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .registeredAt(account.getRegisteredAt())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }
}
