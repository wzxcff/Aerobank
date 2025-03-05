package org.banking.aerobank;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public AuthController(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("User already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            return ResponseEntity.badRequest().body("User does not exist.");
        }
        if (!passwordEncoder.matches(user.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect credentials.");
        }
        return ResponseEntity.ok("Login successful.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody User request_user) {
        if (userRepository.findByEmail(request_user.getEmail()).isEmpty()) {
            return ResponseEntity.badRequest().body("User does not exist.");
        }
        User user = userRepository.findByEmail(request_user.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        jdbcTemplate.execute("ALTER TABLE Users AUTO_INCREMENT = 1");
        return ResponseEntity.ok().body("Deleted user: " + user.getEmail());
    }
}
