package org.banking.aerobank.entities;

import jakarta.persistence.*;
import org.banking.aerobank.security.AESUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;


@Entity
@Table(name = "Cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private double balance;
    private boolean active;


    @Enumerated(EnumType.STRING)
    private CardType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardTypeDesign typeDesign;

    @Column(nullable = false, unique = true, length = 16)
    private String cardNumber;

    @Column(nullable = false)
    private String expDate;

    @Column(name = "cvv", nullable = false)
    private String encryptedCvv;

    @Column(name = "pin", nullable = false)
    private String hashedPin;

    public enum CardType {
        CREDIT,
        DEBIT
    }

    public enum CardTypeDesign {
        GREEN,
        YELLOW,
        BLACK
    }


    public void setCvv(String cvv) {
        this.encryptedCvv = AESUtil.encrypt(cvv); // Encrypt CVV
    }

    public String getCvv() {
        return AESUtil.decrypt(this.encryptedCvv); // Decrypt CVV
    }

    public void setPin(String pin) {
        this.hashedPin = BCrypt.hashpw(pin, BCrypt.gensalt()); // Hash PIN
    }

    public boolean checkPin(String pin) {
        return BCrypt.checkpw(pin, this.hashedPin); // Verify PIN
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CardTypeDesign getTypeDesign() {
        return typeDesign;
    }

    public void setTypeDesign(CardTypeDesign typeDesign) {
        this.typeDesign = typeDesign;
    }

    public String getHashedPin() {
        return hashedPin;
    }

    public void setHashedPin(String hashedPin) {
        this.hashedPin = hashedPin;
    }

    public String getEncryptedCvv() {
        return encryptedCvv;
    }

    public void setEncryptedCvv(String encryptedCvv) {
        this.encryptedCvv = encryptedCvv;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
