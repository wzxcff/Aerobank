package org.banking.aerobank.controllers;

import jakarta.transaction.Transactional;
import org.banking.aerobank.requests.ChangepasswordRequest;
import org.banking.aerobank.repositories.TransactionRepository;
import org.banking.aerobank.requests.User;
import org.banking.aerobank.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionRepository transactionRepository;

    public AuthController(UserRepository userRepository, JdbcTemplate jdbcTemplate, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            return ResponseEntity.badRequest().body("User already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User request_user) {
        Optional<User> optionalUser = userRepository.findByEmail(request_user.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist.");
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(request_user.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials.");
        }
        return ResponseEntity.ok("Login successful.");
    }

    // FIXME: Fix ifs
    @PostMapping("/change_password")
    public ResponseEntity<String> changePassword(@RequestBody ChangepasswordRequest changepasswordRequest) {
        Optional<User> optionalUser = userRepository.findByEmail(changepasswordRequest.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist. Did you write everything right?");
        }

        User user = optionalUser.get();
        if (changepasswordRequest.getNewPassword().equals(changepasswordRequest.getOldPassword())) {
            return ResponseEntity.badRequest().body("Password can't be the same as the old password.");
        }

        if (changepasswordRequest.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters.");
        }

        if (!changepasswordRequest.getNewPassword().equals(changepasswordRequest.getNewPasswordConfirm())) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }

        if (user.getPassword().equals(changepasswordRequest.getNewPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match.");
        }

        user.setPassword(passwordEncoder.encode(changepasswordRequest.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @DeleteMapping("/delete")
    @Transactional
    public ResponseEntity<String> delete(@RequestBody User request_user) {
        Optional<User> optionalUser = userRepository.findByEmail(request_user.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist. Did you write everything right?");
        }
        User user = optionalUser.get();

        transactionRepository.deleteByFromUser(user);
        transactionRepository.deleteByToUser(user);

        userRepository.delete(user);
        jdbcTemplate.execute("ALTER TABLE Users AUTO_INCREMENT = 1");
        return ResponseEntity.ok().body("Deleted user: " + user.getEmail());
    }
}
