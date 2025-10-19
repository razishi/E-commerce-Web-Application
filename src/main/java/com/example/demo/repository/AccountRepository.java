package com.example.demo.repository;

import com.example.demo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Used to look up user during login or registration
    Optional<Account> findByEmail(String email);

    // Used to support login by username
    Optional<Account> findByUsername(String username);

    //  get full object instead of Optional
    Account findByUsernameIgnoreCase(String username);  // For your controller use

    //  Support partial email search for admin user list
    List<Account> findByEmailContainingIgnoreCase(String email);
}
