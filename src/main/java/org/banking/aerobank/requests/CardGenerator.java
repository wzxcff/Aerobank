package org.banking.aerobank.requests;

import org.banking.aerobank.repositories.CardRepository;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CardGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String generateCardNumber(CardRepository cardRepository) {
        String cardNumber;
        do {
            cardNumber = generateRandomCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));
        return cardNumber;
    }

    public static String generateRandomCardNumber() {
        int[] digits = new int[16];

        for (int i = 0; i < 15; i++) {
            digits[i] = random.nextInt(10);
        }

        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int digit = digits[i];
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }

        digits[15] = (10 - (sum % 10)) % 10;

        StringBuilder cardNumber = new StringBuilder();
        for (int digit : digits) {
            cardNumber.append(digit);
        }

        return cardNumber.toString();
    }

    public static String generateCvv() {
        return String.format("%03d", random.nextInt(1000));
    }

    public static String generatePin() {
        return String.format("%04d", random.nextInt(10000));
    }

    public static String generateExpDate() {
        int yearsToAdd = 4;
        LocalDate expiry = LocalDate.now().plusYears(yearsToAdd);
        return expiry.format(DateTimeFormatter.ofPattern("MM/yy"));
    }
}
