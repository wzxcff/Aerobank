package org.banking.aerobank.repositories;

import org.banking.aerobank.entities.Transaction;
import org.banking.aerobank.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromUserOrToUser(User fromUser, User toUser);

    @Query("SELECT t FROM Transaction t WHERE t.fromUser = :user OR t.toUser = :user")
    List<Transaction> findAllByUser(@Param("user") User user);

    void deleteByFromUser(User fromUser);
    void deleteByToUser(User toUser);
}
