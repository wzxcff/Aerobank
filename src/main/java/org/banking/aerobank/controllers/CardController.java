package org.banking.aerobank.controllers;

import org.banking.aerobank.entities.Card;
import org.banking.aerobank.entities.Transaction;
import org.banking.aerobank.entities.User;
import org.banking.aerobank.repositories.CardRepository;
import org.banking.aerobank.repositories.TransactionRepository;
import org.banking.aerobank.repositories.UserRepository;
import org.banking.aerobank.requests.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/account")
public class CardController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public CardController(UserRepository userRepository, TransactionRepository transactionRepository, CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to aerobank";
    }

    @PostMapping("/new_card")
    public ResponseEntity<String> registerNewCard(@RequestBody CardRequest cardRequest) {
        Optional<User> optionalUser = userRepository.findById(cardRequest.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        boolean cardExists = cardRepository.existsByUserIdAndTypeDesign(
                cardRequest.getUserId(),
                Card.CardTypeDesign.valueOf(cardRequest.getTypeDesign().toUpperCase())
        );

        if (cardExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Card already exists");
        }

        User user = optionalUser.get();

        Card newCard = new Card();
        newCard.setUser(user);

        switch (cardRequest.getTypeDesign().toUpperCase()) {
            case "YELLOW":
                newCard.setType(Card.CardType.valueOf("DEBIT"));
                newCard.setTypeDesign(Card.CardTypeDesign.YELLOW);
                break;
            case "GREEN":
                newCard.setType(Card.CardType.valueOf("CREDIT"));
                newCard.setTypeDesign(Card.CardTypeDesign.GREEN);
                break;
            case "BLACK":
                newCard.setType(Card.CardType.valueOf("CREDIT"));
                newCard.setTypeDesign(Card.CardTypeDesign.BLACK);
        }

        newCard.setCardNumber(CardGenerator.generateCardNumber(cardRepository));
        newCard.setExpDate(CardGenerator.generateExpDate());
        newCard.setCvv(CardGenerator.generateCvv());

        String generatedPin = CardGenerator.generatePin();

        newCard.setPin(generatedPin);

        newCard.setActive(true);

        cardRepository.save(newCard);

        return ResponseEntity.status(HttpStatus.CREATED).body("New card created!\nPIN: " + generatedPin);
    }

    @DeleteMapping("/remove_card")
    public ResponseEntity<String> removeCard(@RequestBody RemoveCardRequest removeCardRequest) {
        Optional<Card> optionalCard = cardRepository.findByCardNumber(removeCardRequest.getCardNumber());
        if (optionalCard.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Card not found");
        }
        Card card = optionalCard.get();
        cardRepository.delete(card);
        return ResponseEntity.status(HttpStatus.OK).body("Card removed");
    }

    @GetMapping("/cards")
    public ResponseEntity<?> getCards(@RequestBody GetCardsRequest request) {
        Optional<User> optionalUser = userRepository.findById(request.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        List<Card> cards = cardRepository.findAllByUserId(user.getId());

        List<CardInfo> cardsInfo = cards.stream()
                .map(c -> new CardInfo(
                        c.getCardNumber(),
                        c.getExpDate(),
                        c.getCvv(),
                        c.getTypeDesign().name(),
                        c.getBalance()
                )).toList();

        return ResponseEntity.ok(cardsInfo);
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestBody GetBalanceRequest request) {
        Optional<User> optionalUser = userRepository.findById(request.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        List<Card> cards = cardRepository.findAllByUserId(user.getId());

        List<BalanceInfo> balanceInfo = cards.stream()
                .map(b -> new BalanceInfo(
                        b.getTypeDesign().name(),
                        b.getBalance()
                )).toList();

        return ResponseEntity.ok(balanceInfo);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest withdrawRequest) {
        Optional<User> optionalUser = userRepository.findById(withdrawRequest.getUserId());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        Optional<Card> optionalCard = cardRepository.findByCardNumber(withdrawRequest.getCardNumber());

        if (optionalCard.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Card not found");
        }

        Card card = optionalCard.get();

        if (!card.checkPin(withdrawRequest.getPin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Pin is incorrect");
        }

        if (card.getBalance() < withdrawRequest.getAmount()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }

        card.setBalance(card.getBalance() - withdrawRequest.getAmount());

        Transaction transaction = new Transaction();
        transaction.setFromUser(user);
        transaction.setToUser(null);
        transaction.setAmount(withdrawRequest.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);

        transaction.setFromCard(withdrawRequest.getCardNumber());

        transactionRepository.save(transaction);

        return ResponseEntity.ok("Withdrawn: " + withdrawRequest.getAmount());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest depositRequest) {
        Optional<User> optionalToUser = userRepository.findById(depositRequest.getUserId());

        if (optionalToUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalToUser.get();

        Optional<Card> optionalCard = cardRepository.findByCardNumber(depositRequest.getCardNumber());

        if (optionalCard.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Card not found");
        }

        Card card = optionalCard.get();

        if (!card.checkPin(depositRequest.getPin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Pin is incorrect");
        }

        card.setBalance(card.getBalance() + depositRequest.getAmount());

        Transaction transaction = new Transaction();
        transaction.setToUser(user);
        transaction.setFromUser(null);
        transaction.setAmount(depositRequest.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.DEPOSIT);

        transaction.setToCard(depositRequest.getCardNumber());


        transactionRepository.save(transaction);

        return ResponseEntity.ok("Deposit successful\n\nAmount: " + depositRequest.getAmount());
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> addTransaction(@RequestBody TransferRequest transferRequest) {
        Optional<User> optionalFromUser = userRepository.findById(transferRequest.getFromUserId());
        Optional<User> optionalToUser = userRepository.findById(transferRequest.getFromUserId());

        if (optionalFromUser.isEmpty() || optionalToUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One of the users not found.");
        }

        User fromUser = optionalFromUser.get();
        User toUser = optionalToUser.get();

        Optional<Card> optionalFromCard = cardRepository.findByCardNumber(transferRequest.getFromCardNumber());
        Optional<Card> optionalToCard = cardRepository.findByCardNumber(transferRequest.getToCardNumber());

        if (optionalFromCard.isEmpty() || optionalToCard.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One of the cards not found.");
        }

        Card fromCard = optionalFromCard.get();
        Card toCard = optionalToCard.get();

        if (fromCard.getBalance() < transferRequest.getAmount()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }

        fromCard.setBalance(fromCard.getBalance() - transferRequest.getAmount());
        toCard.setBalance(toCard.getBalance() + transferRequest.getAmount());

        Transaction transaction = new Transaction();
        transaction.setFromUser(fromUser);
        transaction.setAmount(transferRequest.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.TRANSFER);

        transaction.setFromCard(fromCard.getCardNumber());
        transaction.setToCard(toCard.getCardNumber());

        transactionRepository.save(transaction);
        return ResponseEntity.ok("Transaction completed successfully\nFrom: " + fromUser.getEmail() + "\nTo: " + toUser.getEmail() + "\nAmount: " + transferRequest.getAmount());
    }

    @GetMapping("/transactions/{email}")
    public ResponseEntity<List<TransactionInfo>> getTransactions(@PathVariable String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
        }

        User user = optionalUser.get();

        List<Transaction> transactions = transactionRepository.findAllByUser(user);

        List<TransactionInfo> transactionInfos = transactions.stream()
                .map(t -> new TransactionInfo(
                        t.getFromUser() != null ? t.getFromUser().getEmail() : "N/A",
                        t.getToUser() != null ? t.getToUser().getEmail() : "N/A",
                        t.getAmount(),
                        t.getType().toString(),
                        t.getTimestamp()
                ))
                .toList();

        return ResponseEntity.ok(transactionInfos);
    }
}
