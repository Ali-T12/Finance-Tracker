package controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import model.CategorySummary;
import model.Transaction;
import service.FileManager;
import util.Session;

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

    @FXML
    public void initialize() {
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        loadReport();
    }

    private void loadReport() {

        List<Transaction> transactions = FileManager.loadTransactions()
                .stream()
                .filter(t -> t.getUserId() == Session.currentUser.getId())
                .collect(Collectors.toList());

        List<Category> categories = FileManager.loadCategories()
                .stream()
                .filter(c -> c.getUserId() == Session.currentUser.getId())
                .collect(Collectors.toList());

        double totalIncome = transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("Income"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpense = transactions.stream()
                .filter(t -> t.getType().equalsIgnoreCase("Expense"))
                .mapToDouble(Transaction::getAmount)
                .sum();

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

        Map<Integer, Double> totalsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategoryId,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        List<CategorySummary> summaryList = totalsByCategory.entrySet()
                .stream()
                .map(entry -> {
                    String categoryName = categories.stream()
                            .filter(c -> c.getId() == entry.getKey())
                            .map(Category::getName)
                            .findFirst()
                            .orElse("Unknown");

                    return new CategorySummary(categoryName, entry.getValue());
                })
                .collect(Collectors.toList());

        categorySummaryTable.setItems(FXCollections.observableArrayList(summaryList));
    }
}