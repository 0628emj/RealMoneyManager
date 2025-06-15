package com.ledger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseService {
    private List<Expense> expenses;

    public ExpenseService() {
        this.expenses = new ArrayList<>();
        //샘플.
        addSampleData();
    }

    private void addSampleData() {
        try {
            expenses.add(new Expense(LocalDate.now().minusDays(2), Category.식비, 15000, "점심 식사"));
            expenses.add(new Expense(LocalDate.now().minusDays(1), Category.교통, 2500, "버스 요금"));
            expenses.add(new Expense(LocalDate.now(), Category.쇼핑, 50000, "옷 구매"));
            expenses.add(new Expense(LocalDate.now().minusMonths(1).withDayOfMonth(5), Category.공과금, 70000, "전기세"));
            expenses.add(new Expense(LocalDate.now().minusMonths(1).withDayOfMonth(15), Category.통신, 55000, "휴대폰 요금"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
        sortExpensesByDate();
    }

    public void deleteExpense(String id) {
        this.expenses.removeIf(expense -> expense.getId().equals(id));
    }

    public void updateExpense(String id, LocalDate date, Category category, int amount, String memo) {
        for (Expense expense : expenses) {
            if (expense.getId().equals(id)) {
                expense.setDate(date);
                expense.setCategory(category);
                expense.setAmount(amount);
                expense.setMemo(memo);
                sortExpensesByDate();
                return;
            }
        }
    }

    public List<Expense> getAllExpenses() {
        return new ArrayList<>(this.expenses);
    }

    public Expense getExpenseById(String id) {
        return this.expenses.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Expense> getRecentExpenses(int count) {
        sortExpensesByDate();
        return expenses.stream().limit(count).collect(Collectors.toList());
    }

    public List<Expense> getExpensesByMonth(int year, int month) {
        return expenses.stream()
                .filter(e -> e.getDate().getYear() == year && e.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    public List<Expense> getExpensesByPeriod(LocalDate startDate, LocalDate endDate) {
        return expenses.stream()
                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    private void sortExpensesByDate() {
        //최신 날짜가 위로
        Collections.sort(this.expenses, Comparator.comparing(Expense::getDate).reversed());
    }

    //분석용
    public Map<Category, Integer> getCategoryWiseSum(List<Expense> expenseList) {
        return expenseList.stream()
                .collect(Collectors.groupingBy(Expense::getCategory,
                        Collectors.summingInt(Expense::getAmount)));
    }

    public int getTotalAmount(List<Expense> expenseList) {
        return expenseList.stream().mapToInt(Expense::getAmount).sum();
    }
}