package org.banking.aerobank.requests;

public class CardInfo {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String typeDesign;

    private double balance;

    public CardInfo(String cardNumber, String expiryDate, String cvv, String typeDesign, double balance) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.typeDesign = typeDesign;
        this.balance = balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getTypeDesign() {
        return typeDesign;
    }

    public void setTypeDesign(String typeDesign) {
        this.typeDesign = typeDesign;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
