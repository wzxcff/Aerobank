package org.banking.aerobank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/account")
public class AccountController {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountController(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to aerobank";
    }

    @GetMapping("/balance")
    public ResponseEntity<String> getBalance(@RequestBody User request_user) {
        Optional<User> optionalUser = userRepository.findByEmail(request_user.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        return ResponseEntity.ok("Balance: " + user.getBalance());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest withdrawRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(withdrawRequest.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = optionalUser.get();

        if (user.getBalance() < withdrawRequest.getAmount()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }
        user.setBalance(user.getBalance() - withdrawRequest.getAmount());
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setFromUser(user);
        transaction.setToUser(null);
        transaction.setAmount(withdrawRequest.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);

        transactionRepository.save(transaction);

        return ResponseEntity.ok("Withdrawn: " + withdrawRequest.getAmount());
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest depositRequest) {
        Optional<User> optionalToUser = userRepository.findByEmail(depositRequest.getEmail());

        if (optionalToUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User toUser = optionalToUser.get();

        toUser.setBalance(toUser.getBalance() + depositRequest.getAmount());
        userRepository.save(toUser);


        Transaction transaction = new Transaction();
        transaction.setToUser(toUser);
        transaction.setFromUser(null);
        transaction.setAmount(depositRequest.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.DEPOSIT);

        transactionRepository.save(transaction);

        return ResponseEntity.ok("Deposit successful\n\nTo: " + toUser.getEmail() + "\nAmount: " + depositRequest.getAmount());
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> addTransaction(@RequestBody TransferRequest transferRequest) {
        Optional<User> optionalFromUser = userRepository.findByEmail(transferRequest.getFromEmail());
        Optional<User> optionalToUser = userRepository.findByEmail(transferRequest.getToEmail());

        if (optionalFromUser.isEmpty() || optionalToUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("One of the users not found.");
        }

        User fromUser = optionalFromUser.get();
        User toUser = optionalToUser.get();

        if (fromUser.getBalance() < transferRequest.getAmount()) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }

        fromUser.setBalance(fromUser.getBalance() - transferRequest.getAmount());
        toUser.setBalance(toUser.getBalance() + transferRequest.getAmount());

        Transaction transaction = new Transaction();
        transaction.setFromUser(fromUser);
        transaction.setToUser(toUser);
        transaction.setAmount(transferRequest.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setType(Transaction.TransactionType.TRANSFER);

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
