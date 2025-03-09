package org.banking.aerobank.controllers;

import org.banking.aerobank.entities.Card;
import org.banking.aerobank.entities.Transaction;
import org.banking.aerobank.entities.User;
import org.banking.aerobank.repositories.CardRepository;
import org.banking.aerobank.repositories.TransactionRepository;
import org.banking.aerobank.repositories.UserRepository;
import org.banking.aerobank.requests.*;
import org.banking.aerobank.security.AESUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/account")
public class AccountController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public AccountController(UserRepository userRepository, TransactionRepository transactionRepository, CardRepository cardRepository) {
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
        newCard.setType(Card.CardType.valueOf(cardRequest.getType().toUpperCase()));
        newCard.setTypeDesign(Card.CardTypeDesign.valueOf(cardRequest.getTypeDesign().toUpperCase()));

        newCard.setCardNumber(CardGenerator.generateCardNumber(cardRepository));
        newCard.setExpDate(CardGenerator.generateExpDate());
        newCard.setCvv(CardGenerator.generateCvv());
        newCard.setPin(CardGenerator.generatePin());

        newCard.setActive(true);

        cardRepository.save(newCard);

        return ResponseEntity.status(HttpStatus.CREATED).body("New card created");
    }

    // TODO: Create remove card method

    // TEST
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


//    // FIXME
//    @PostMapping("/withdraw")
//    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest withdrawRequest) {
//        Optional<User> optionalUser = userRepository.findByEmail(withdrawRequest.getEmail());
//
//        if (optionalUser.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//
//        User user = optionalUser.get();
//
//        if (user.getBalance() < withdrawRequest.getAmount()) {
//            return ResponseEntity.badRequest().body("Not enough balance");
//        }
//        user.setBalance(user.getBalance() - withdrawRequest.getAmount());
//        userRepository.save(user);
//
//        Transaction transaction = new Transaction();
//        transaction.setFromUser(user);
//        transaction.setToUser(null);
//        transaction.setAmount(withdrawRequest.getAmount());
//        transaction.setTimestamp(LocalDateTime.now());
//        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
//
//        transactionRepository.save(transaction);
//
//        return ResponseEntity.ok("Withdrawn: " + withdrawRequest.getAmount());
//    }
//
//    // FIXME
//    @PostMapping("/deposit")
//    public ResponseEntity<String> deposit(@RequestBody DepositRequest depositRequest) {
//        Optional<User> optionalToUser = userRepository.findByEmail(depositRequest.getEmail());
//
//        if (optionalToUser.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//
//        User toUser = optionalToUser.get();
//
//        toUser.setBalance(toUser.getBalance() + depositRequest.getAmount());
//        userRepository.save(toUser);
//
//
//        Transaction transaction = new Transaction();
//        transaction.setToUser(toUser);
//        transaction.setFromUser(null);
//        transaction.setAmount(depositRequest.getAmount());
//        transaction.setTimestamp(LocalDateTime.now());
//        transaction.setType(Transaction.TransactionType.DEPOSIT);
//
//        transactionRepository.save(transaction);
//
//        return ResponseEntity.ok("Deposit successful\n\nTo: " + toUser.getEmail() + "\nAmount: " + depositRequest.getAmount());
//    }
//
//    // FIXME
//    @PostMapping("/transaction")
//    public ResponseEntity<String> addTransaction(@RequestBody TransferRequest transferRequest) {
//        Optional<User> optionalFromUser = userRepository.findByEmail(transferRequest.getFromEmail());
//        Optional<User> optionalToUser = userRepository.findByEmail(transferRequest.getToEmail());
//
//        if (optionalFromUser.isEmpty() || optionalToUser.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One of the users not found.");
//        }
//
//        User fromUser = optionalFromUser.get();
//        User toUser = optionalToUser.get();
//
//        if (fromUser.getBalance() < transferRequest.getAmount()) {
//            return ResponseEntity.badRequest().body("Not enough balance");
//        }
//
//        fromUser.setBalance(fromUser.getBalance() - transferRequest.getAmount());
//        toUser.setBalance(toUser.getBalance() + transferRequest.getAmount());
//
//        Transaction transaction = new Transaction();
//        transaction.setFromUser(fromUser);
//        transaction.setToUser(toUser);
//        transaction.setAmount(transferRequest.getAmount());
//        transaction.setTimestamp(LocalDateTime.now());
//        transaction.setType(Transaction.TransactionType.TRANSFER);
//
//        transactionRepository.save(transaction);
//        return ResponseEntity.ok("Transaction completed successfully\nFrom: " + fromUser.getEmail() + "\nTo: " + toUser.getEmail() + "\nAmount: " + transferRequest.getAmount());
//    }

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
