package org.banking.aerobank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

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
        User user = userRepository.findByEmail(request_user.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok("Balance: " + user.getBalance());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest withdrawRequest) {
        User user = userRepository.findByEmail(withdrawRequest.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

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
        User toUser = userRepository.findByEmail(depositRequest.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
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
        User fromUser = userRepository.findByEmail(transferRequest.getFromEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        User toUser = userRepository.findByEmail(transferRequest.getToEmail()).orElseThrow(() -> new RuntimeException("User not found"));

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
    public ResponseEntity<List<String>> getTransactions(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findAllByUser(user);
        List<String> transactionsDetails = transactions.stream()
                .map(t -> "From: " + (t.getFromUser() != null ? t.getFromUser().getEmail() : "N/A") +
                        " To: " + (t.getToUser() != null ? t.getToUser().getEmail() : "N/A") +
                        " Amount: $" + t.getAmount() +
                        " Type: " + t.getType()
                )
                .toList();        return ResponseEntity.ok(transactionsDetails);
    }
}
