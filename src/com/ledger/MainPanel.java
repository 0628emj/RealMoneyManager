package com.ledger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainPanel extends JPanel {
    private MainApp mainApp;

    public MainPanel(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel titleLabel = new JLabel("가계부", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        gbc.weighty = 0.2; //위쪽 공간 여유
        add(titleLabel, gbc);

        gbc.weighty = 0; //가중치

        JButton btnAddExpense = new JButton("지출 추가");
        btnAddExpense.setPreferredSize(new Dimension(200, 80));
        btnAddExpense.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnAddExpense.addActionListener(e -> mainApp.showPanel(MainApp.ADD_EXPENSE_PANEL));
        gbc.ipady = 40;
        add(btnAddExpense, gbc);

        gbc.ipady = 0;

        Dimension smallButtonSize = new Dimension(200, 50);
        Font smallButtonFont = new Font("SansSerif", Font.PLAIN, 16);

        JButton btnDeleteExpense = new JButton("지출 삭제");
        btnDeleteExpense.setPreferredSize(smallButtonSize);
        btnDeleteExpense.setFont(smallButtonFont);
        btnDeleteExpense.addActionListener(e -> mainApp.showPanel(MainApp.LIST_EXPENSES_PANEL_FOR_DELETE));
        add(btnDeleteExpense, gbc);

        JButton btnModifyExpense = new JButton("지출 수정");
        btnModifyExpense.setPreferredSize(smallButtonSize);
        btnModifyExpense.setFont(smallButtonFont);
        btnModifyExpense.addActionListener(e -> mainApp.showPanel(MainApp.LIST_EXPENSES_PANEL_FOR_MODIFY));
        add(btnModifyExpense, gbc);

        JButton btnViewExpenses = new JButton("지출 조회");
        btnViewExpenses.setPreferredSize(smallButtonSize);
        btnViewExpenses.setFont(smallButtonFont);
        btnViewExpenses.addActionListener(e -> mainApp.showPanel(MainApp.VIEW_MONTHLY_PANEL));
        add(btnViewExpenses, gbc);

        JButton btnAnalyzeExpenses = new JButton("지출 분석");
        btnAnalyzeExpenses.setPreferredSize(smallButtonSize);
        btnAnalyzeExpenses.setFont(smallButtonFont);
        btnAnalyzeExpenses.addActionListener(e -> mainApp.showPanel(MainApp.ANALYZE_PERIOD_PANEL));
        gbc.weighty = 0.2; //아래쪽 공간
        add(btnAnalyzeExpenses, gbc);
    }
}