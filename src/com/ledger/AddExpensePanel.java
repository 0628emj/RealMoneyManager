package com.ledger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AddExpensePanel extends JPanel {
    private MainApp mainApp;
    private ExpenseService expenseService;

    private JSpinner dateSpinner;
    private JComboBox<Category> categoryComboBox;
    private JTextArea memoArea;
    private JButton btnRegister, btnCancel;

    private Map<Integer, JLabel> amountCountLabels;
    private Map<Integer, Integer> amountCounts;
    private JLabel totalAmountLabel;

    private final int[] denominations = {1000, 10000, 50000, 100000, 500000};

    public AddExpensePanel(MainApp mainApp, ExpenseService expenseService) {
        this.mainApp = mainApp;
        this.expenseService = expenseService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //상단 입력 패널
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        //날짜
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("날짜:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START; gbc.weightx = 1.0;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        formPanel.add(dateSpinner, gbc);
        gbc.weightx = 0;

        //카테고리
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("카테고리:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        categoryComboBox = new JComboBox<>(Category.values());
        formPanel.add(categoryComboBox, gbc);

        //금액 버튼 패널
        JPanel amountButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        amountCountLabels = new HashMap<>();
        amountCounts = new HashMap<>();

        for (int denom : denominations) {
            amountCounts.put(denom, 0);
            JButton btn = new JButton(String.format("%,d원", denom));
            JLabel countLabel = new JLabel("x0");
            amountCountLabels.put(denom, countLabel);
            btn.addActionListener(e -> {
                amountCounts.put(denom, amountCounts.get(denom) + 1);
                updateAmountDisplay();
            });
            amountButtonPanel.add(btn);
            amountButtonPanel.add(countLabel);
        }
        JButton btnResetAmount = new JButton("금액 초기화");
        btnResetAmount.addActionListener(e -> resetAmounts());
        amountButtonPanel.add(btnResetAmount);


        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("금액:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        totalAmountLabel = new JLabel("총액: 0원");
        totalAmountLabel.setFont(totalAmountLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(totalAmountLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(amountButtonPanel, gbc);
        gbc.gridwidth = 1;


        //메모
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.NORTHEAST; //위쪽 정렬
        formPanel.add(new JLabel("메모 (20자 이내):"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH; //수직수평 모두 채움
        gbc.weighty = 1.0; //메모 영역이 남은 공간 차지하도록
        memoArea = new JTextArea(3, 20);
        memoArea.setLineWrap(true);
        memoArea.setWrapStyleWord(true);
        // 20자 제한
        memoArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (memoArea.getText().length() >= 20 && memoArea.getSelectedText() == null) {
                    e.consume(); //입력 막기
                }
            }
        });
        JScrollPane memoScrollPane = new JScrollPane(memoArea);
        formPanel.add(memoScrollPane, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;


        add(formPanel, BorderLayout.CENTER);

        //하단 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRegister = new JButton("등록");
        btnCancel = new JButton("취소");

        btnRegister.addActionListener(e -> registerExpense());
        btnCancel.addActionListener(e -> {
            resetForm();
            mainApp.showPanel(MainApp.MAIN_PANEL);
        });

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateAmountDisplay() {
        int total = 0;
        for (int denom : denominations) {
            int count = amountCounts.get(denom);
            amountCountLabels.get(denom).setText("x" + count);
            total += denom * count;
        }
        totalAmountLabel.setText(String.format("총액: %,d원", total));
    }

    private void resetAmounts() {
        for (int denom : denominations) {
            amountCounts.put(denom, 0);
        }
        updateAmountDisplay();
    }

    private void registerExpense() {
        java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
        LocalDate date = utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        Category category = (Category) categoryComboBox.getSelectedItem();
        String memo = memoArea.getText().trim();

        int totalAmount = 0;
        for (Map.Entry<Integer, Integer> entry : amountCounts.entrySet()) {
            totalAmount += entry.getKey() * entry.getValue();
        }

        if (totalAmount <= 0) {
            JOptionPane.showMessageDialog(this, "금액을 입력해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (memo.length() > 20) {
             JOptionPane.showMessageDialog(this, "메모는 20자 이내로 작성해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }


        Expense newExpense = new Expense(date, category, totalAmount, memo);
        expenseService.addExpense(newExpense);

        JOptionPane.showMessageDialog(this, "지출이 등록되었습니다.");
        resetForm();
        mainApp.showPanel(MainApp.MAIN_PANEL);
    }

    private void resetForm() {
        dateSpinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        categoryComboBox.setSelectedIndex(0);
        memoArea.setText("");
        resetAmounts();
    }
}