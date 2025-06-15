package com.ledger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewMonthlyPanel extends JPanel {
    private MainApp mainApp;
    private ExpenseService expenseService;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private static final String SELECT_MONTH_VIEW = "SelectMonthView";
    private static final String CALENDAR_VIEW = "CalendarView";
    private static final String ANALYSIS_VIEW = "AnalysisView";

    private JComboBox<Integer> yearSelector;
    private JComboBox<MonthItem> monthSelector;

    private JTable calendarTable;
    private DefaultTableModel calendarTableModel;

    private JLabel currentSelectedMonthLabel_Calendar;

    private JTextArea analysisArea;
    private JLabel currentSelectedMonthLabel_Analysis;
    private JLabel totalForAnalysisLabel; //총지출
    private JLabel avgForAnalysisLabel;   //일 평균

    private static final String[] DAYS_OF_WEEK = {"일", "월", "화", "수", "목", "금", "토"};

    private int currentYear;
    private int currentMonth;

    static class MonthItem {
        int monthValue; String monthName;
        public MonthItem(int monthValue, String monthName) {
            this.monthValue = monthValue; this.monthName = monthName;
        }
        @Override public String toString() { return monthName; }
    }

    public ViewMonthlyPanel(MainApp mainApp, ExpenseService expenseService) {
        this.mainApp = mainApp;
        this.expenseService = expenseService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        JPanel selectMonthPanel = createSelectMonthPanel();
        contentPanel.add(selectMonthPanel, SELECT_MONTH_VIEW);

        JPanel calendarViewPanel = createCalendarViewPanel();
        contentPanel.add(calendarViewPanel, CALENDAR_VIEW);

        JPanel analysisViewPanel = createAnalysisViewPanel();
        contentPanel.add(analysisViewPanel, ANALYSIS_VIEW);

        add(contentPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBackToMain = new JButton("메인으로 돌아가기");
        btnBackToMain.addActionListener(e -> {
            cardLayout.show(contentPanel, SELECT_MONTH_VIEW);
            mainApp.showPanel(MainApp.MAIN_PANEL);
        });
        bottomPanel.add(btnBackToMain);
        add(bottomPanel, BorderLayout.SOUTH);

        cardLayout.show(contentPanel, SELECT_MONTH_VIEW);
    }

    
    
    private JPanel createSelectMonthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("조회할 년/월 선택"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        LocalDate today = LocalDate.now();
        yearSelector = new JComboBox<>();
        for (int y = today.getYear() - 5; y <= today.getYear() + 5; y++) {
            yearSelector.addItem(y);
        }
        yearSelector.setSelectedItem(today.getYear());

        monthSelector = new JComboBox<>();
        String[] monthNames = {"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
        for(int i=0; i<12; i++) {
            monthSelector.addItem(new MonthItem(i+1, monthNames[i]));
        }
        monthSelector.setSelectedIndex(today.getMonthValue() - 1);

        JButton btnShowCalendar = new JButton("달력으로 조회하기");
        btnShowCalendar.addActionListener(e -> {
            currentYear = (Integer) yearSelector.getSelectedItem();
            MonthItem selectedMonthItem = (MonthItem) monthSelector.getSelectedItem();
            if (selectedMonthItem != null) {
                currentMonth = selectedMonthItem.monthValue;
                updateCalendarView();
                currentSelectedMonthLabel_Calendar.setText(String.format("%d년 %d월 지출 내역", currentYear, currentMonth));
                cardLayout.show(contentPanel, CALENDAR_VIEW);
            }
        });

        panel.add(new JLabel("년:"), gbc);
        gbc.gridx++; panel.add(yearSelector, gbc);
        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("월:"), gbc);
        gbc.gridx++; panel.add(monthSelector, gbc);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnShowCalendar, gbc);

        return panel;
    }

    //달력조회
    private JPanel createCalendarViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("월별 지출 달력"));

        currentSelectedMonthLabel_Calendar = new JLabel(" ", SwingConstants.CENTER);
        currentSelectedMonthLabel_Calendar.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(currentSelectedMonthLabel_Calendar, BorderLayout.NORTH);

        calendarTableModel = new DefaultTableModel(DAYS_OF_WEEK, 0) {
             @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        calendarTable = new JTable(calendarTableModel);
        calendarTable.setRowHeight(60);
        calendarTable.getTableHeader().setReorderingAllowed(false);
        calendarTable.setDefaultRenderer(Object.class, new CalendarCellRenderer());
        JScrollPane calendarScrollPane = new JScrollPane(calendarTable);
        panel.add(calendarScrollPane, BorderLayout.CENTER);
        


        JPanel bottomControls = new JPanel(new BorderLayout());


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnShowAnalysis = new JButton("이 달 분석하기");
        btnShowAnalysis.addActionListener(e -> {
            performMonthlyAnalysis();
            currentSelectedMonthLabel_Analysis.setText(String.format("%d년 %d월 지출 분석", currentYear, currentMonth));
            cardLayout.show(contentPanel, ANALYSIS_VIEW);
        });
        JButton btnBackToSelect = new JButton("월 선택으로");
        btnBackToSelect.addActionListener(e -> cardLayout.show(contentPanel, SELECT_MONTH_VIEW));
        
        buttonPanel.add(btnBackToSelect);
        buttonPanel.add(btnShowAnalysis);
        bottomControls.add(buttonPanel, BorderLayout.CENTER); 
        panel.add(bottomControls, BorderLayout.SOUTH);

        return panel;
    }

    //월별 분석패널
    private JPanel createAnalysisViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createTitledBorder("월별 지출 분석 결과"));


        JPanel topInfoPanel = new JPanel(new BorderLayout());
        currentSelectedMonthLabel_Analysis = new JLabel(" ", SwingConstants.CENTER);
        currentSelectedMonthLabel_Analysis.setFont(new Font("Serif", Font.BOLD, 18));
        topInfoPanel.add(currentSelectedMonthLabel_Analysis, BorderLayout.NORTH);

        JPanel summaryForAnalysisPanel = new JPanel(new GridLayout(2,1)); //요약 정보용
        summaryForAnalysisPanel.setBorder(BorderFactory.createEmptyBorder(5,0,10,0)); //여백
        totalForAnalysisLabel = new JLabel("총 지출: -", SwingConstants.CENTER);
        totalForAnalysisLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        avgForAnalysisLabel = new JLabel("일 평균 지출: -", SwingConstants.CENTER);
        avgForAnalysisLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        summaryForAnalysisPanel.add(totalForAnalysisLabel);
        summaryForAnalysisPanel.add(avgForAnalysisLabel);
        topInfoPanel.add(summaryForAnalysisPanel, BorderLayout.CENTER);

        panel.add(topInfoPanel, BorderLayout.NORTH);


        analysisArea = new JTextArea();
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane analysisScrollPane = new JScrollPane(analysisArea);
        panel.add(analysisScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnBackToCalendar = new JButton("달력으로 돌아가기");
        btnBackToCalendar.addActionListener(e -> cardLayout.show(contentPanel, CALENDAR_VIEW));
        buttonPanel.add(btnBackToCalendar);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    //달력 업데이트
    private void updateCalendarView() {
        if (currentYear == 0 || currentMonth == 0) return; 

        List<Expense> monthlyExpenses = expenseService.getExpensesByMonth(currentYear, currentMonth);
        Map<Integer, Integer> dailyTotals = new HashMap<>();
        for (Expense expense : monthlyExpenses) {
            dailyTotals.merge(expense.getDate().getDayOfMonth(), expense.getAmount(), Integer::sum);
        }

        calendarTableModel.setRowCount(0);
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int firstDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7; 

        int daysInMonth = yearMonth.lengthOfMonth();
        String[] rowData = new String[7];

        for (int i = 0; i < firstDayOfWeekValue; i++) {
            rowData[i] = "";
        }

        for (int day = 1; day <= daysInMonth; day++) {
            int col = (firstDayOfWeekValue + day - 1) % 7;
            rowData[col] = String.valueOf(day);
            Integer total = dailyTotals.get(day);
            if (total != null) {
                rowData[col] += String.format("\n(%,d)", total);
            }

            if (col == 6 || day == daysInMonth) {
                calendarTableModel.addRow(rowData);
                rowData = new String[7];
            }
        }
        while (calendarTableModel.getRowCount() < 6) {
            calendarTableModel.addRow(new String[7]);
        }
        

    }


    private void performMonthlyAnalysis() {
        if (currentYear == 0 || currentMonth == 0) return;

        List<Expense> monthlyExpenses = expenseService.getExpensesByMonth(currentYear, currentMonth);
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth); //월 일수계산
        int daysInMonth = yearMonth.lengthOfMonth();

        if (monthlyExpenses.isEmpty()) {
            totalForAnalysisLabel.setText("총 지출: 0원");
            avgForAnalysisLabel.setText(String.format("일 평균 지출: 0원 (총 %d일)", daysInMonth));
            analysisArea.setText("해당 월의 지출 내역이 없습니다.");
            return;
        }

        int totalAmount = expenseService.getTotalAmount(monthlyExpenses);
        double avgDailyExpense = 0;
        if (daysInMonth > 0 && totalAmount > 0) {
            avgDailyExpense = (double) totalAmount / daysInMonth;
        }


        totalForAnalysisLabel.setText(String.format("총 지출: %,d원", totalAmount));
        avgForAnalysisLabel.setText(String.format("일 평균 지출: %,.0f원 (총 %d일)", avgDailyExpense, daysInMonth));


        //카태고리별 분석 내용
        Map<Category, Integer> categorySums = expenseService.getCategoryWiseSum(monthlyExpenses);
        StringBuilder sb = new StringBuilder();

        sb.append("카테고리별 지출:\n");

        int maxAmountForGraph = categorySums.values().stream().max(Integer::compareTo).orElse(1);
        for (Map.Entry<Category, Integer> entry : categorySums.entrySet()) {
            sb.append(String.format("%-5s: %,10d원 ", entry.getKey().name(), entry.getValue()));
            int barLength = (int) (((double)entry.getValue() / maxAmountForGraph) * 20);
            for(int i=0; i < barLength; i++) sb.append("*");
            sb.append("\n");
        }
        analysisArea.setText(sb.toString());
        analysisArea.setCaretPosition(0);
    }


    static class CalendarCellRenderer extends DefaultTableCellRenderer {
        public CalendarCellRenderer() {
            setVerticalAlignment(SwingConstants.TOP);
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value != null) {
                setText("<html>" + value.toString().replace("\n", "<br>") + "</html>");
            } else {
                setText("");
            }
            if (col == 0) setForeground(Color.RED);
            else if (col == 6) setForeground(Color.BLUE);
            else setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            return this;
        }
    }
}