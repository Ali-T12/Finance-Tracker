/**
 *علي حمال اسعيد 120220484
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import model.Transaction;
import util.AlertUtil;
import util.DatabaseConnection;
import util.Session;

/**
 *
 * @author Ali
 */
public class TransactionsController {

    @FXML
    private TextField searchCategoryField;

    @FXML
    private DatePicker filterDatePicker;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, Integer> idColumn;

    @FXML
    private TableColumn<Transaction, Integer> userIdColumn;

    @FXML
    private TableColumn<Transaction, Integer> categoryIdColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private List<Category> categories = new ArrayList<>();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadCategoriesFromDatabase();
        loadTransactionsFromDatabase();

        typeComboBox.setItems(FXCollections.observableArrayList("Income", "Expense"));
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Amount Ascending",
                "Amount Descending",
                "Date Ascending",
                "Date Descending"
        ));

        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, selected) -> {
                    if (selected != null) {
                        amountField.setText(String.valueOf(selected.getAmount()));
                        typeComboBox.setValue(selected.getType());
                        datePicker.setValue(LocalDate.parse(selected.getDate()));

                        for (Category c : categories) {
                            if (c.getId() == selected.getCategoryId()) {
                                categoryComboBox.setValue(c.getName());
                                break;
                            }
                        }
                    }
                }
        );
    }

    private void loadCategoriesFromDatabase() {
        categories.clear();
        categoryComboBox.getItems().clear();
        String query = "SELECT * FROM categories WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, Session.currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category c = new Category(rs.getInt("id"), rs.getInt("user_id"), rs.getString("name"));
                    categories.add(c);
                    categoryComboBox.getItems().add(c.getName());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Failed to load categories.");
        }
    }

    private void loadTransactionsFromDatabase() {
        transactions.clear();
        String query = "SELECT * FROM transactions WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
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
            showError("Failed to load transactions.");
        }
        transactionTable.setItems(transactions);
    }

    @FXML
    private void handleAddTransaction() {
        if (datePicker.getValue() == null) {
            showError("Please select a date.");
            return;
        }

        if (datePicker.getValue().isAfter(LocalDate.now())) {
            showError("Future date is not allowed.");
            return;
        }

        if (amountField.getText().isEmpty()
                || categoryComboBox.getValue() == null
                || typeComboBox.getValue() == null) {
            showError("Fill all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (Exception e) {
            showError("Invalid amount.");
            return;
        }

        if (amount <= 0) {
            showError("Amount must be positive.");
            return;
        }

        String categoryName = categoryComboBox.getValue();
        int categoryId = categories.stream()
                .filter(c -> c.getName().equals(categoryName))
                .findFirst()
                .get()
                .getId();

        String query = "INSERT INTO transactions (user_id, category_id, amount, type, date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, Session.currentUser.getId());
            ps.setInt(2, categoryId);
            ps.setDouble(3, amount);
            ps.setString(4, typeComboBox.getValue());
            ps.setString(5, datePicker.getValue().toString());
            ps.executeUpdate();

            loadTransactionsFromDatabase();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to save transaction to database.");
        }
    }

    @FXML
    private void handleEditTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select a transaction first.");
            return;
        }

        if (datePicker.getValue() == null) {
            showError("Please select a date.");
            return;
        }

        if (datePicker.getValue().isAfter(LocalDate.now())) {
            showError("Future date is not allowed.");
            return;
        }

        if (amountField.getText().isEmpty()
                || categoryComboBox.getValue() == null
                || typeComboBox.getValue() == null) {
            showError("Fill all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
        } catch (Exception e) {
            showError("Invalid amount.");
            return;
        }

        if (amount <= 0) {
            showError("Amount must be positive.");
            return;
        }

        String categoryName = categoryComboBox.getValue();
        int categoryId = categories.stream()
                .filter(c -> c.getName().equals(categoryName))
                .findFirst()
                .get()
                .getId();

        String query = "UPDATE transactions SET category_id = ?, amount = ?, type = ?, date = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, categoryId);
            ps.setDouble(2, amount);
            ps.setString(3, typeComboBox.getValue());
            ps.setString(4, datePicker.getValue().toString());
            ps.setInt(5, selected.getId());
            ps.setInt(6, Session.currentUser.getId());
            ps.executeUpdate();

            selected.setAmount(amount);
            selected.setType(typeComboBox.getValue());
            selected.setDate(datePicker.getValue().toString());
            selected.setCategoryId(categoryId);

            transactionTable.refresh();
            clearFields();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to update transaction.");
        }
    }

    @FXML
    private void handleSearchTransaction() {
        String keyword = searchCategoryField.getText().trim();
        LocalDate selectedDate = filterDatePicker.getValue();

        if (keyword.isEmpty() && selectedDate == null) {
            showError("Please enter a category or select a date.");
            return;
        }

        List<Transaction> result = transactions.stream()
                .filter(t -> {
                    boolean matchesCategory = true;
                    boolean matchesDate = true;

                    if (!keyword.isEmpty()) {
                        String categoryName = categories.stream()
                                .filter(c -> c.getId() == t.getCategoryId())
                                .map(Category::getName)
                                .findFirst()
                                .orElse("");

                        matchesCategory = categoryName.toLowerCase().contains(keyword.toLowerCase());
                    }

                    if (selectedDate != null) {
                        matchesDate = t.getDate().equals(selectedDate.toString());
                    }

                    return matchesCategory && matchesDate;
                })
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            showError("No transactions found.");
            return;
        }

        transactionTable.setItems(FXCollections.observableArrayList(result));
    }

    @FXML
    private void handleSortTransaction() {
        String sortOption = sortComboBox.getValue();

        if (sortOption == null) {
            showError("Please select a sorting option.");
            return;
        }

        List<Transaction> sortedList;

        switch (sortOption) {
            case "Amount Ascending":
                sortedList = transactions.stream()
                        .sorted(Comparator.comparingDouble(Transaction::getAmount))
                        .collect(Collectors.toList());
                break;

            case "Amount Descending":
                sortedList = transactions.stream()
                        .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                        .collect(Collectors.toList());
                break;

            case "Date Ascending":
                sortedList = transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getDate))
                        .collect(Collectors.toList());
                break;

            default:
                sortedList = transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getDate).reversed())
                        .collect(Collectors.toList());
                break;
        }

        transactionTable.setItems(FXCollections.observableArrayList(sortedList));
    }

    @FXML
    private void handleResetTransaction() {
        transactionTable.setItems(transactions);
        searchCategoryField.clear();
        filterDatePicker.setValue(null);
        sortComboBox.setValue(null);
    }

    private void clearFields() {
        amountField.clear();
        categoryComboBox.setValue(null);
        typeComboBox.setValue(null);
        datePicker.setValue(null);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}