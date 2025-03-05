package org.banking.aerobank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("api/account")
public class AccountController {

    private final UserRepository userRepository;

    public AccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
