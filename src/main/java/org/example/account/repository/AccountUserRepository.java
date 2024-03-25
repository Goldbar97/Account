package org.example.account.repository;

import org.example.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUserRepository extends JpaRepository<AccountUser,
        Long> {
}
