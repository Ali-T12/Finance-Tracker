/**
 * علي حمال اسعيد 120220484
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CategorySummary;
import model.Transaction;
import repository.TransactionRepository;

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
    private TableView<CategorySummary> categorySummaryTable;

    @FXML
    private TableColumn<CategorySummary, String> categoryNameColumn;

    @FXML
    private TableColumn<CategorySummary, Double> categoryTotalColumn;

    private TransactionRepository transactionRepository = new TransactionRepository();

    @FXML
    public void initialize() {
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        loadReport();
    }

    private void loadReport() {
        List<Transaction> transactions = transactionRepository.findAll();

        if (transactions == null) {
            showError("Failed to load transactions for report.");
            return;
        }

        double totalIncome = transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("Income"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("Expense"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double balance = totalIncome - totalExpense;

        incomeLabel.setText(String.format("%.2f", totalIncome));
        expenseLabel.setText(String.format("%.2f", totalExpense));
        balanceLabel.setText(String.format("%.2f", balance));

        if (balance > 0) {
            summaryLabel.setText("Good saving");
        } else if (balance < 0) {
            summaryLabel.setText("More expenses than income");
        } else {
            summaryLabel.setText("Balanced");
        }

        Map<String, Double> totalsByCategory = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        List<CategorySummary> summaryList = totalsByCategory.entrySet()
                .stream()
                .map(entry -> new CategorySummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        categorySummaryTable.setItems(FXCollections.observableArrayList(summaryList));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}