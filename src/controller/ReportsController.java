package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import model.Category;
import model.CategorySummary;
import model.Transaction;
import util.DatabaseConnection;
import util.Session;

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
        List<Transaction> transactions = new ArrayList<>();
        List<Category> categories = new ArrayList<>();

        String transactionQuery = "SELECT * FROM transactions WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(transactionQuery)) {
            
            ps.setInt(1, Session.currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("category_id"),
                            rs.getDouble("amount"),
                            rs.getString("type"),
                            rs.getDate("date").toString()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load transactions for report.");
            return;
        }

        String categoryQuery = "SELECT * FROM categories WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(categoryQuery)) {
            
            ps.setInt(1, Session.currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load categories for report.");
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}