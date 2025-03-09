package org.banking.aerobank.requests;

public class BalanceInfo {
    private String typeDesign;
    private double balance;

    public BalanceInfo(String typeDesign, double balance) {
        this.typeDesign = typeDesign;
        this.balance = balance;
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
