package org.banking.aerobank;


import java.time.LocalDateTime;

public class TransactionInfo {
    private String fromEmail;
    private String toEmail;
    private double amount;
    private String type;
    private LocalDateTime timestamp;

    public TransactionInfo(String fromEmail, String toEmail, double amount, String type, LocalDateTime timestamp) {
        this.fromEmail = fromEmail;
        this.toEmail = toEmail;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
