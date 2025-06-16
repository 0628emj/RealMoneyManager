package com.ledger;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class AnalyzePeriodPanel extends JPanel {
    private MainApp mainApp;
    private ExpenseService expenseService;

    private JSpinner startDateSpinner, endDateSpinner;
    private JRadioButton radioTable, radioGraph;
    private JTextArea tableResultArea;
    private CombinedGraphPanel graphDisplayContainerPanel;
    private CardLayout resultCardLayout;
    private JPanel resultContainerPanel;

    private static final String TABLE_VIEW = "TableView";
    private static final String GRAPH_VIEW = "GraphView";

    //그래프용 데이터
    private String periodStringForGraph = "";
    private String totalAmountStringForGraph = "";
    private String avgDailyExpenseStringForGraph = "";
    private Map<Category, Integer> categorySumsForGraph = new HashMap<>();
    private int totalAmountValueForGraph = 0;
    private List<Color> categoryColors;
    private List<SegmentExpense> segmentExpensesForGraph = new ArrayList<>();


    static class SegmentExpense {
        String label;
        int amount;
        SegmentExpense(String label, int amount) {
            this.label = label;
            this.amount = amount;
        }
    }

    public AnalyzePeriodPanel(MainApp mainApp, ExpenseService expenseService) {
        this.mainApp = mainApp;
        this.expenseService = expenseService;
        categoryColors = generateCategoryColors();
        clearGraphData();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //삳단
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("분석 시작일:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        SpinnerDateModel startDateModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startDateModel);
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd"));
        startDateSpinner.setValue(java.util.Date.from(LocalDate.now().minusMonths(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        controlPanel.add(startDateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("분석 종료일:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        endDateSpinner = new JSpinner(endDateModel);
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));
        endDateSpinner.setValue(java.util.Date.from(LocalDate.now().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
        controlPanel.add(endDateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        controlPanel.add(new JLabel("표시 방법:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        radioTable = new JRadioButton("표", true);
        radioGraph = new JRadioButton("그래프");
        ButtonGroup group = new ButtonGroup();
        group.add(radioTable);
        group.add(radioGraph);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        radioPanel.add(radioTable);
        radioPanel.add(radioGraph);
        controlPanel.add(radioPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton btnAnalyze = new JButton("분석 시작");
        btnAnalyze.addActionListener(e -> performAnalysis());
        controlPanel.add(btnAnalyze, gbc);
        add(controlPanel, BorderLayout.NORTH);



        resultCardLayout = new CardLayout();
        resultContainerPanel = new JPanel(resultCardLayout);

        tableResultArea = new JTextArea();
        tableResultArea.setEditable(false);
        tableResultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPaneTable = new JScrollPane(tableResultArea);
        resultContainerPanel.add(scrollPaneTable, TABLE_VIEW);

        graphDisplayContainerPanel = new CombinedGraphPanel();
        JScrollPane scrollPaneGraph = new JScrollPane(graphDisplayContainerPanel);
        scrollPaneGraph.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPaneGraph.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultContainerPanel.add(scrollPaneGraph, GRAPH_VIEW);


        add(resultContainerPanel, BorderLayout.CENTER);
        resultCardLayout.show(resultContainerPanel, TABLE_VIEW);

        //하단
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBack = new JButton("메인으로 돌아가기");
        btnBack.addActionListener(e -> {
            tableResultArea.setText("");
            clearGraphData();
            if (graphDisplayContainerPanel != null) {
                graphDisplayContainerPanel.updateDataAndRefresh(); //데이터 업데이트
            }
            resultCardLayout.show(resultContainerPanel, TABLE_VIEW);
            mainApp.showPanel(MainApp.MAIN_PANEL);
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

    }
    
    private void clearGraphData() {
        periodStringForGraph = "분석 전";
        totalAmountStringForGraph = "총 지출: -";
        avgDailyExpenseStringForGraph = "일 평균: -";
        if (categorySumsForGraph != null) categorySumsForGraph.clear(); else categorySumsForGraph = new HashMap<>();
        totalAmountValueForGraph = 0;
        if (segmentExpensesForGraph != null) segmentExpensesForGraph.clear(); else segmentExpensesForGraph = new ArrayList<>();
    }

    private List<Color> generateCategoryColors() {
        List<Color> colors = new ArrayList<>();
        colors.add(new Color(255, 99, 132));  //빨
        colors.add(new Color(54, 162, 235));  //파
        colors.add(new Color(255, 206, 86));  //노
        colors.add(new Color(75, 192, 192));  //초
        colors.add(new Color(153, 102, 255)); //보
        colors.add(new Color(255, 159, 64));  //주황
        colors.add(new Color(199, 199, 199)); //회
        colors.add(new Color(83, 102, 255)); //indigo
        Random rand = new Random(0); 
        while (colors.size() < Category.values().length) {
            colors.add(new Color(rand.nextInt(200), rand.nextInt(200), rand.nextInt(200)));
        }
        return colors;
    }

    private void performAnalysis() {
        java.util.Date utilStartDate = (java.util.Date) startDateSpinner.getValue();
        LocalDate startDate = utilStartDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.util.Date utilEndDate = (java.util.Date) endDateSpinner.getValue();
        LocalDate endDate = utilEndDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this, "시작일은 종료일보다 이전이어야 합니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Expense> periodExpenses = expenseService.getExpensesByPeriod(startDate, endDate);
        clearGraphData(); 

        StringBuilder sb = new StringBuilder(); 
        if (periodExpenses.isEmpty()) {
            sb.append("해당 기간의 지출 내역이 없습니다.");
            periodStringForGraph = String.format("%s ~ %s (데이터 없음)", startDate, endDate);
            totalAmountStringForGraph = "총 지출: 0원";
            avgDailyExpenseStringForGraph = "일 평균: 0원";
        } else {
            periodStringForGraph = String.format("분석 기간: %s ~ %s", startDate, endDate);
            sb.append(periodStringForGraph).append("\n");
            sb.append("========================================\n");

            long daysInPeriod = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            totalAmountValueForGraph = expenseService.getTotalAmount(periodExpenses);
            double averageDailyExpense = (daysInPeriod > 0) ? (double) totalAmountValueForGraph / daysInPeriod : 0;

            totalAmountStringForGraph = String.format("총 소비 금액: %,d원", totalAmountValueForGraph);
            avgDailyExpenseStringForGraph = String.format("일 평균 지출 금액: %,.0f원 (총 %d일)", averageDailyExpense, daysInPeriod);
            
            sb.append(totalAmountStringForGraph).append("\n");
            sb.append(avgDailyExpenseStringForGraph).append("\n");
            sb.append("----------------------------------------\n");

            sb.append("카테고리별 총 지출 금액 및 비중:\n");
            categorySumsForGraph = expenseService.getCategoryWiseSum(periodExpenses);
            categorySumsForGraph = categorySumsForGraph.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            int maxCategoryNameLength = Category.values().length > 0 ? 
                                        java.util.Arrays.stream(Category.values()).map(c->c.name().length()).max(Integer::compareTo).get() : 5;
            for (Map.Entry<Category, Integer> entry : categorySumsForGraph.entrySet()) {
                double percentage = (totalAmountValueForGraph > 0) ? ((double) entry.getValue() / totalAmountValueForGraph) * 100 : 0;
                sb.append(String.format("- %-"+maxCategoryNameLength+"s: %,10d원 (%.1f%%)\n", entry.getKey().name(), entry.getValue(), percentage));
            }
            sb.append("----------------------------------------\n");

            sb.append("구간별 지출 금액 (기간 10분할):\n");
            if (daysInPeriod > 0) {
                int numSegments = 10;
                long segmentLengthDays = Math.max(1, daysInPeriod / numSegments);
                if (daysInPeriod < numSegments) numSegments = (int) daysInPeriod;

                LocalDate loopCurrentSegmentStart = startDate; 
                for (int i = 0; i < numSegments; i++) {
                    final LocalDate segmentStartForLambda = loopCurrentSegmentStart;
                    LocalDate segmentEndForLambda = loopCurrentSegmentStart.plusDays(segmentLengthDays - 1);

                    if (i == numSegments - 1) { 
                        segmentEndForLambda = endDate;
                    }
                    if (segmentEndForLambda.isAfter(endDate)) {
                        segmentEndForLambda = endDate;
                    }
                    final LocalDate finalSegmentEndForLambda = segmentEndForLambda;

                    List<Expense> segmentExpensesData = periodExpenses.stream()
                            .filter(e -> !e.getDate().isBefore(segmentStartForLambda) && !e.getDate().isAfter(finalSegmentEndForLambda))
                            .collect(Collectors.toList());
                    int segmentTotal = expenseService.getTotalAmount(segmentExpensesData);
                    
                    String segmentGraphLabel = String.format("구간%d", i + 1);
                    String segmentTableLabel = String.format("구간 %d (%s~%s)", i + 1, segmentStartForLambda.toString().substring(5), finalSegmentEndForLambda.toString().substring(5));

                    segmentExpensesForGraph.add(new SegmentExpense(segmentGraphLabel, segmentTotal));
                    sb.append(String.format("- %s: %,d원\n", segmentTableLabel, segmentTotal));
                    
                    loopCurrentSegmentStart = finalSegmentEndForLambda.plusDays(1); 
                    if(loopCurrentSegmentStart.isAfter(endDate)) break;
                }
            } else {
                 sb.append("- 기간이 너무 짧아 구간 분석이 어렵습니다.\n");
            }
            sb.append("========================================\n");
        }
        
        tableResultArea.setText(sb.toString());
        tableResultArea.setCaretPosition(0);

        if (radioTable.isSelected()) {
            resultCardLayout.show(resultContainerPanel, TABLE_VIEW);
        } else if (radioGraph.isSelected()) {
            if (graphDisplayContainerPanel != null) {
                graphDisplayContainerPanel.updateDataAndRefresh(); //데이터 업데이트
            }
            resultCardLayout.show(resultContainerPanel, GRAPH_VIEW);
        }
    }


    private class CombinedGraphPanel extends JPanel {
        private final int PADDING = 30;
        private final int SUMMARY_HEIGHT = 70; 
        private final int GRAPH_SPACING = 30;  
        private final int TITLE_HEIGHT = 20;   
        private final int BAR_CHART_MIN_HEIGHT = 150; 

        private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 13);
        private final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
        private final Font SUMMARY_FONT = new Font("SansSerif", Font.BOLD, 14);
        private final Font EMPTY_DATA_FONT = new Font("SansSerif", Font.PLAIN, 12);

        private String currentPeriodStr;
        private String currentTotalAmountStr;
        private String currentAvgDailyStr;
        private Map<Category, Integer> currentCategorySums;
        private int currentTotalAmountVal;
        private List<SegmentExpense> currentSegmentExpenses;

        CombinedGraphPanel() {
            setBackground(Color.WHITE);
            updateDataAndRefresh(); //초기 데이터로 설정
        }

        public void updateDataAndRefresh() {
            //AnalyzePeriodPanel의 멤버 변수에서 최신 데이터 가져와서 업데이트
            this.currentPeriodStr = AnalyzePeriodPanel.this.periodStringForGraph;
            this.currentTotalAmountStr = AnalyzePeriodPanel.this.totalAmountStringForGraph;
            this.currentAvgDailyStr = AnalyzePeriodPanel.this.avgDailyExpenseStringForGraph;
            //동시수정방지
            this.currentCategorySums = new LinkedHashMap<>(AnalyzePeriodPanel.this.categorySumsForGraph);
            this.currentTotalAmountVal = AnalyzePeriodPanel.this.totalAmountValueForGraph;
            this.currentSegmentExpenses = new ArrayList<>(AnalyzePeriodPanel.this.segmentExpensesForGraph);
            
            revalidate(); //크기 재계산요청
            repaint();    //다시 그리기 요청
        }


        @Override
        public Dimension getPreferredSize() {
            int calculatedHeight = SUMMARY_HEIGHT; 

            boolean hasCategoryData = currentCategorySums != null && !currentCategorySums.isEmpty();
            boolean hasSegmentData = currentSegmentExpenses != null && !currentSegmentExpenses.isEmpty();

            if (hasCategoryData) {
                calculatedHeight += TITLE_HEIGHT + BAR_CHART_MIN_HEIGHT + GRAPH_SPACING;
            }
            if (hasSegmentData) {
                calculatedHeight += TITLE_HEIGHT + BAR_CHART_MIN_HEIGHT + PADDING; 
            }
            
            if (!hasCategoryData && !hasSegmentData && currentPeriodStr != null && currentPeriodStr.contains("데이터 없음")){
                 calculatedHeight += 100; 
            } else if (!hasCategoryData && !hasSegmentData) {
                 calculatedHeight += 50; //최소높이
            }


            int parentWidth = getParent() instanceof JViewport ? getParent().getWidth() : super.getPreferredSize().width;
            if(parentWidth <=0) parentWidth = 600; 

            return new Dimension(parentWidth, calculatedHeight);
        }
        

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth = getWidth(); 
            int currentY = PADDING;

            g2.setColor(Color.BLACK);
            g2.setFont(SUMMARY_FONT);
            if (currentPeriodStr != null && !currentPeriodStr.isEmpty()) {
                g2.drawString(currentPeriodStr, PADDING, currentY); currentY += 20;
            }
            if (currentTotalAmountStr != null && !currentTotalAmountStr.isEmpty()) {
                g2.drawString(currentTotalAmountStr, PADDING, currentY); currentY += 20;
            }
            if (currentAvgDailyStr != null && !currentAvgDailyStr.isEmpty()) {
                g2.drawString(currentAvgDailyStr, PADDING, currentY);
            }
            currentY = SUMMARY_HEIGHT + PADDING/2;

            boolean hasCategoryData = currentCategorySums != null && !currentCategorySums.isEmpty();
            boolean hasSegmentData = currentSegmentExpenses != null && !currentSegmentExpenses.isEmpty();

            if (!hasCategoryData && !hasSegmentData) {
                g2.setFont(EMPTY_DATA_FONT);
                String noDataMsg = "표시할 그래프 데이터가 없습니다. (또는 분석 전)";
                if(currentPeriodStr != null && currentPeriodStr.contains("데이터 없음")) {
                    noDataMsg = "해당 기간에 지출 내역이 없습니다.";
                }
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(noDataMsg, (panelWidth - fm.stringWidth(noDataMsg)) / 2, currentY + 50);
                return;
            }

            int availableGraphWidth = panelWidth - 2 * PADDING;

            if (hasCategoryData) {
                drawCategoryBarGraph(g2, PADDING, currentY, availableGraphWidth, BAR_CHART_MIN_HEIGHT);
                currentY += BAR_CHART_MIN_HEIGHT + TITLE_HEIGHT + GRAPH_SPACING;
            }

            if (hasSegmentData) {
                drawSegmentBarGraph(g2, PADDING, currentY, availableGraphWidth, BAR_CHART_MIN_HEIGHT);
            }
        }

        private void drawCategoryBarGraph(Graphics2D g2, int x, int y, int width, int allocatedHeight) {
            final int LEGEND_AREA_WIDTH = Math.min(180, width / 3);
            int graphDrawableWidth = width - LEGEND_AREA_WIDTH - 10;
            int graphDrawableHeight = allocatedHeight - TITLE_HEIGHT - PADDING/2; 

            if (graphDrawableWidth <= 20 || graphDrawableHeight <= 20 || currentCategorySums.isEmpty()) return;

            g2.setColor(Color.BLACK);
            g2.setFont(TITLE_FONT);
            g2.drawString("카테고리별 지출 비율", x, y + TITLE_HEIGHT - 5); 

            int chartStartY = y + TITLE_HEIGHT;
            g2.drawLine(x, chartStartY, x, chartStartY + graphDrawableHeight); 
            g2.drawLine(x, chartStartY + graphDrawableHeight, x + graphDrawableWidth, chartStartY + graphDrawableHeight); 

            int barCount = currentCategorySums.size();
            int barSpacing = 5;
            int barWidth = (graphDrawableWidth - (barCount -1) * barSpacing) / barCount;
            if (barWidth < 5) barWidth = 5;

            int currentX = x;
            int legendX = x + graphDrawableWidth + 10;
            int legendY = chartStartY;
            int legendItemHeight = 15;
            int colorIndex = 0;
            FontMetrics fm = g2.getFontMetrics(LABEL_FONT); //금액 표시용

            for (Map.Entry<Category, Integer> entry : currentCategorySums.entrySet()) {
                if (currentX + barWidth > x + graphDrawableWidth + 1) break; 

                Category category = entry.getKey();
                int value = entry.getValue(); //실제 지출 금액
                double percentage = (currentTotalAmountVal > 0) ? (double) value / currentTotalAmountVal : 0.0;
                int barH = (int) (percentage * graphDrawableHeight);
                if (barH < 1 && value > 0) barH = 1;

                Color barColor = categoryColors.get(category.ordinal() % categoryColors.size());
                g2.setColor(barColor);
                g2.fillRect(currentX, chartStartY + graphDrawableHeight - barH, barWidth, barH);
                

                if (value > 0) {
                    String amountStr = String.format("%,d", value);
                    int amountStrWidth = fm.stringWidth(amountStr);
                    int amountStrHeight = fm.getAscent();

                    //조건담.문자열 너비가 막대 너비보다 작고 높이가 글자 높이보다 클때
             
                        g2.setColor(Color.BLACK); // 글자색
                        g2.drawString(amountStr, currentX + (barWidth - amountStrWidth) / 2, chartStartY + graphDrawableHeight - barH - 2);
                    
                }
                //표시 추가 끝


                if (legendY + (colorIndex + 1) * legendItemHeight < chartStartY + graphDrawableHeight + PADDING) { 
                    g2.setColor(barColor);
                    g2.fillRect(legendX, legendY + colorIndex * legendItemHeight, 8, 8);
                    g2.setColor(Color.BLACK);
                    g2.setFont(LABEL_FONT);
                    g2.drawString(String.format("%s: %.1f%%", category.name(), percentage * 100), legendX + 12, legendY + colorIndex * legendItemHeight + 8);
                }
                currentX += barWidth + barSpacing;
                colorIndex++;
            }
        }

        private void drawSegmentBarGraph(Graphics2D g2, int x, int y, int width, int allocatedHeight) {
            int graphDrawableHeight = allocatedHeight - TITLE_HEIGHT - PADDING/2 - 15; 
            int xAxisLabelSpace = 15;

            if (graphDrawableHeight <= 20 || currentSegmentExpenses.isEmpty()) return;

            g2.setColor(Color.BLACK);
            g2.setFont(TITLE_FONT);
            g2.drawString("구간별 지출 금액", x, y + TITLE_HEIGHT - 5);

            int chartStartY = y + TITLE_HEIGHT;
            g2.drawLine(x, chartStartY, x, chartStartY + graphDrawableHeight); 
            g2.drawLine(x, chartStartY + graphDrawableHeight, x + width - PADDING, chartStartY + graphDrawableHeight); 

            int maxSegmentAmount = currentSegmentExpenses.stream().mapToInt(s -> s.amount).max().orElse(1);
            if (maxSegmentAmount == 0) maxSegmentAmount = 1;

            int barCount = currentSegmentExpenses.size();
            int barSpacing = 5;
            int barWidth = (width - PADDING - (barCount-1) * barSpacing) / barCount;
            if (barWidth < 5) barWidth = 5;

            int currentX = x;
            Color segmentBarColor = new Color(70, 130, 180);
            FontMetrics fm = g2.getFontMetrics(LABEL_FONT);

            for (SegmentExpense se : currentSegmentExpenses) {
                if (currentX + barWidth > x + width - PADDING + 1) break;

                int barH = (int) (((double)se.amount / maxSegmentAmount) * graphDrawableHeight);
                if (barH < 1 && se.amount > 0) barH = 1;

                g2.setColor(segmentBarColor);
                g2.fillRect(currentX, chartStartY + graphDrawableHeight - barH, barWidth, barH);
                
                g2.setColor(Color.BLACK);
                g2.setFont(LABEL_FONT);
                String labelShort = se.label; 
                int labelWidth = fm.stringWidth(labelShort);
                g2.drawString(labelShort, currentX + (barWidth - labelWidth)/2 , chartStartY + graphDrawableHeight + xAxisLabelSpace - 3);


                if (se.amount > 0) {
                    String amountStr = String.format("%,d", se.amount);
                    int amountStrWidth = fm.stringWidth(amountStr);
                    int amountStrHeight = fm.getAscent();

                    //문자열 너비가 막대 너비보다 작거나 같아야 함. + 막대 높이가 금액 문자열 높이보다 커야 위에 쓸 공간이 있음

                       g2.drawString(amountStr, currentX + (barWidth - amountStrWidth)/2, chartStartY + graphDrawableHeight - barH - 2);
                    

                }
                currentX += barWidth + barSpacing;
            }
        }
    }
}