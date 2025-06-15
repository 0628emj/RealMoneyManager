package com.ledger;

import java.time.LocalDate;
import java.util.UUID;

public class Expense {
    private String id;
    private LocalDate date;
    private Category category;
    private int amount;
    private String memo;

    public Expense(LocalDate date, Category category, int amount, String memo) {
        this.id = UUID.randomUUID().toString(); // 고유 ID생성
        this.date = date;
        this.category = category;
        this.amount = amount;
        this.memo = memo;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public Category getCategory() { return category; }
    public int getAmount() { return amount; }
    public String getMemo() { return memo; }


    public void setDate(LocalDate date) { this.date = date; }
    public void setCategory(Category category) { this.category = category; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setMemo(String memo) { this.memo = memo; }

    @Override
    public String toString() {
        return String.format("%s | %s | %,d원 | %s", date, category, amount, memo);
    }
}