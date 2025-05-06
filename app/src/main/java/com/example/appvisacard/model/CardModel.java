package com.example.appvisacard.model;

public class CardModel {
    private int id;
    private String cardNumber;
    private String expireDate;
    private String cardHolder;

    public CardModel(int id, String cardNumber, String expireDate, String cardHolder) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.expireDate = expireDate;
        this.cardHolder = cardHolder;
    }

    public int getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public String getExpireDate() { return expireDate; }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }
}
