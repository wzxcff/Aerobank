package org.banking.aerobank.repositories;

import org.banking.aerobank.entities.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByUserId(int userId);
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findAllByUserId(int userId);


    boolean existsByUserIdAndTypeDesign(int userId, Card.CardTypeDesign typeDesign);
    boolean existsByCardNumber(String cardNumber);
}
