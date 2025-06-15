package com.ledger;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanelContainer;
    private ExpenseService expenseService;


    public static final String MAIN_PANEL = "MainPanel";
    public static final String ADD_EXPENSE_PANEL = "AddExpensePanel";
    public static final String LIST_EXPENSES_PANEL_FOR_DELETE = "ListExpensesPanelForDelete";
    public static final String LIST_EXPENSES_PANEL_FOR_MODIFY = "ListExpensesPanelForModify";
    public static final String VIEW_MONTHLY_PANEL = "ViewMonthlyPanel";
    public static final String ANALYZE_PERIOD_PANEL = "AnalyzePeriodPanel";


    public MainApp() {
        expenseService = new ExpenseService();
        cardLayout = new CardLayout();
        mainPanelContainer = new JPanel(cardLayout);


        MainPanel mainP = new MainPanel(this);
        AddExpensePanel addP = new AddExpensePanel(this, expenseService);
        ListExpensesPanel deleteP = new ListExpensesPanel(this, expenseService, ListExpensesPanel.Mode.DELETE);
        ListExpensesPanel modifyP = new ListExpensesPanel(this, expenseService, ListExpensesPanel.Mode.MODIFY);
        ViewMonthlyPanel viewP = new ViewMonthlyPanel(this, expenseService);
        AnalyzePeriodPanel analyzeP = new AnalyzePeriodPanel(this, expenseService);


        mainPanelContainer.add(mainP, MAIN_PANEL);
        mainPanelContainer.add(addP, ADD_EXPENSE_PANEL);
        mainPanelContainer.add(deleteP, LIST_EXPENSES_PANEL_FOR_DELETE);
        mainPanelContainer.add(modifyP, LIST_EXPENSES_PANEL_FOR_MODIFY);
        mainPanelContainer.add(viewP, VIEW_MONTHLY_PANEL);
        mainPanelContainer.add(analyzeP, ANALYZE_PERIOD_PANEL);


        add(mainPanelContainer);

        setTitle("간단 가계부");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null); //화면 중앙에
        setVisible(true);

        showPanel(MAIN_PANEL); //메인 패널
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanelContainer, panelName);

        if (panelName.equals(LIST_EXPENSES_PANEL_FOR_DELETE)) {
            ListExpensesPanel panel = (ListExpensesPanel) getPanelByName(LIST_EXPENSES_PANEL_FOR_DELETE);
            if(panel != null) panel.refreshList();
        } else if (panelName.equals(LIST_EXPENSES_PANEL_FOR_MODIFY)) {
             ListExpensesPanel panel = (ListExpensesPanel) getPanelByName(LIST_EXPENSES_PANEL_FOR_MODIFY);
            if(panel != null) panel.refreshList();
        }
    }
    
    private Component getPanelByName(String name) {
        for (Component comp : mainPanelContainer.getComponents()) {
            if (mainPanelContainer.getComponentZOrder(comp) != -1) {
                if (comp instanceof ListExpensesPanel && name.equals(LIST_EXPENSES_PANEL_FOR_DELETE) && ((ListExpensesPanel)comp).getMode() == ListExpensesPanel.Mode.DELETE) return comp;
                if (comp instanceof ListExpensesPanel && name.equals(LIST_EXPENSES_PANEL_FOR_MODIFY) && ((ListExpensesPanel)comp).getMode() == ListExpensesPanel.Mode.MODIFY) return comp;

            }
        }
        return null;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp());
    }
}