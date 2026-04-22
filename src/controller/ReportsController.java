/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import model.Transaction;
import service.FileManager;

/**
 *
 * @author Ali
 */
public class ReportsController {
     @FXML
    private Label incomeLabel;

    @FXML
    private Label expenseLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    public void initialize() {
        loadReport();
    }

    private void loadReport() {

        List<Transaction> transactions = FileManager.loadTransactions();

        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("Income")) {
                totalIncome += t.getAmount();
            } else if (t.getType().equalsIgnoreCase("Expense")) {
                totalExpense += t.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        incomeLabel.setText(String.valueOf(totalIncome));
        expenseLabel.setText(String.valueOf(totalExpense));
        balanceLabel.setText(String.valueOf(balance));

        if (balance > 0) {
            summaryLabel.setText("Good saving");
        } else if (balance < 0) {
            summaryLabel.setText("More expenses than income");
        } else {
            summaryLabel.setText("Balanced");
        }
    }
}
