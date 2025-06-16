package com.ledger;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ModifyExpenseDialog extends JDialog {
    private ExpenseService expenseService;
    private Expense currentExpense;
    private ListExpensesPanel parentPanel; //목록 갱신

    private JSpinner dateSpinner;
    private JComboBox<Category> categoryComboBox;
    private JTextField amountField;
    private JTextArea memoArea;

    public ModifyExpenseDialog(Frame owner, ExpenseService expenseService, Expense expense, ListExpensesPanel parentPanel) {
        super(owner, "지출 수정", true);
        this.expenseService = expenseService;
        this.currentExpense = expense;
        this.parentPanel = parentPanel;

        setSize(450, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


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
        formPanel.add(dateSpinner, gbc);
        gbc.weightx = 0;

        //카테고리
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("카테고리:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START;
        categoryComboBox = new JComboBox<>(Category.values());
        formPanel.add(categoryComboBox, gbc);

        //금액
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        formPanel.add(new JLabel("금액:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        amountField = new JTextField(15);
        formPanel.add(amountField, gbc);

        //메모
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("메모 (20자 이내):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        memoArea = new JTextArea(3, 20);
        memoArea.setLineWrap(true);
        memoArea.setWrapStyleWord(true);
        memoArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (memoArea.getText().length() >= 20 && memoArea.getSelectedText() == null) {
                    e.consume();
                }
            }
        });
        JScrollPane memoScrollPane = new JScrollPane(memoArea);
        formPanel.add(memoScrollPane, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add(formPanel, BorderLayout.CENTER);


        fillData();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("저장");
        JButton btnCancel = new JButton("취소");

        btnSave.addActionListener(e -> saveChanges());
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fillData() {
        if (currentExpense != null) {
            dateSpinner.setValue(java.util.Date.from(currentExpense.getDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
            categoryComboBox.setSelectedItem(currentExpense.getCategory());
            amountField.setText(String.valueOf(currentExpense.getAmount()));
            memoArea.setText(currentExpense.getMemo());
        }
    }

    private void saveChanges() {
        java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
        LocalDate date = utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        Category category = (Category) categoryComboBox.getSelectedItem();
        String memo = memoArea.getText().trim();
        int amount;

        try {
            amount = Integer.parseInt(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "금액은 0보다 커야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "유효한 금액을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (memo.length() > 20) {
             JOptionPane.showMessageDialog(this, "메모는 20자 이내로 작성해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        expenseService.updateExpense(currentExpense.getId(), date, category, amount, memo);
        JOptionPane.showMessageDialog(this, "지출 내역이 수정되었습니다.");
        parentPanel.refreshList();
        dispose();
    }
}