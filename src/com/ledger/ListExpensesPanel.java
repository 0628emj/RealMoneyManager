package com.ledger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class ListExpensesPanel extends JPanel {
    public enum Mode { DELETE, MODIFY }

    private MainApp mainApp;
    private ExpenseService expenseService;
    private Mode mode;
    private JTable expenseTable;
    private DefaultTableModel tableModel;

    public ListExpensesPanel(MainApp mainApp, ExpenseService expenseService, Mode mode) {
        this.mainApp = mainApp;
        this.expenseService = expenseService;
        this.mode = mode;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(mode == Mode.DELETE ? "지출 삭제 (최근 10개)" : "지출 수정 (최근 10개)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "날짜", "카테고리", "금액", "메모", "액션"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        expenseTable = new JTable(tableModel);
        expenseTable.setRowHeight(30);
        expenseTable.getColumnModel().getColumn(0).setMinWidth(0);
        expenseTable.getColumnModel().getColumn(0).setMaxWidth(0);
        expenseTable.getColumnModel().getColumn(0).setWidth(0);



        String actionButtonText = mode == Mode.DELETE ? "삭제" : "수정";
        expenseTable.getColumn("액션").setCellRenderer(new ButtonRenderer(actionButtonText));
        expenseTable.getColumn("액션").setCellEditor(new ButtonEditor(new JCheckBox(), actionButtonText, this::handleAction));


        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton btnBack = new JButton("메인으로 돌아가기");
        btnBack.addActionListener(e -> mainApp.showPanel(MainApp.MAIN_PANEL));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshList();
    }

    public Mode getMode() {
        return mode;
    }

    public void refreshList() {
        tableModel.setRowCount(0);
        List<Expense> recentExpenses = expenseService.getRecentExpenses(10);
        for (Expense expense : recentExpenses) {
            Object[] rowData = {
                    expense.getId(),
                    expense.getDate().toString(),
                    expense.getCategory().name(),
                    String.format("%,d원", expense.getAmount()),
                    expense.getMemo(),
                    mode == Mode.DELETE ? "삭제" : "수정"
            };
            tableModel.addRow(rowData);
        }
    }

    private void handleAction(int rowIndex) {
        String expenseId = (String) tableModel.getValueAt(rowIndex, 0);
        if (expenseId == null) return;

        if (mode == Mode.DELETE) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "정말로 이 지출 내역을 삭제하시겠습니까?", "삭제 확인",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                expenseService.deleteExpense(expenseId);
                refreshList();
                JOptionPane.showMessageDialog(this, "삭제되었습니다.");
            }
        } else if (mode == Mode.MODIFY) {
            Expense expenseToModify = expenseService.getExpenseById(expenseId);
            if (expenseToModify != null) {
                ModifyExpenseDialog dialog = new ModifyExpenseDialog(mainApp, expenseService, expenseToModify, this);
                dialog.setVisible(true);
            }
        }
    }

    //jtable렌ㄷ더
    static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setOpaque(true);
            setText(text);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    //에디터
    static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private ActionHandler actionHandler;
        private int selectedRow;

        interface ActionHandler {
            void execute(int row);
        }

        public ButtonEditor(JCheckBox checkBox, String text, ActionHandler handler) {
            super(checkBox);
            this.actionHandler = handler;
            button = new JButton(text);
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.selectedRow = row;
            button.setText((value == null) ? "" : value.toString());
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                actionHandler.execute(selectedRow);
            }
            isPushed = false;
            return button.getText();
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}