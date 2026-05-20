/**
 * علي حمال اسعيد 120220484
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import repository.CategoryRepository;
import repository.TransactionRepository;
import util.AlertUtil;

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
    private TableColumn<Transaction, String> categoryIdColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private List<Category> categories = new ArrayList<>();

    private CategoryRepository categoryRepository = new CategoryRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        userIdColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getUser().getId())
        );

        categoryIdColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getCategory().getName())
        );

        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        loadCategories();
        loadTransactions();

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

                        if (selected.getCategory() != null) {
                            categoryComboBox.setValue(selected.getCategory().getName());
                        }
                    }
                }
        );
    }

    private void loadCategories() {
        categories.clear();
        categoryComboBox.getItems().clear();

        List<Category> categoryList = categoryRepository.findAll();

        if (categoryList != null) {
            categories.addAll(categoryList);

            for (Category c : categories) {
                categoryComboBox.getItems().add(c.getName());
            }
        }
    }

    private void loadTransactions() {
        transactions.clear();

        List<Transaction> transactionList = transactionRepository.findAll();

        if (transactionList != null) {
            transactions.addAll(transactionList);
        }

        transactionTable.setItems(transactions);
    }

    @FXML
    private void handleAddTransaction() {
        if (!validateTransactionInput()) {
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());
        int categoryId = getSelectedCategoryId();

        if (categoryId == -1) {
            showError("Selected category not found.");
            return;
        }

        Transaction addedTransaction = transactionRepository.add(
                categoryId,
                amount,
                typeComboBox.getValue(),
                datePicker.getValue().toString()
        );

        if (addedTransaction == null) {
            showError("Failed to save transaction.");
            return;
        }

        loadTransactions();
        clearFields();
    }

    @FXML
    private void handleEditTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select a transaction first.");
            return;
        }

        if (!validateTransactionInput()) {
            return;
        }

        double amount = Double.parseDouble(amountField.getText().trim());
        int categoryId = getSelectedCategoryId();

        if (categoryId == -1) {
            showError("Selected category not found.");
            return;
        }

        Transaction updatedTransaction = transactionRepository.update(
                selected.getId(),
                categoryId,
                amount,
                typeComboBox.getValue(),
                datePicker.getValue().toString()
        );

        if (updatedTransaction == null) {
            showError("Failed to update transaction.");
            return;
        }

        loadTransactions();
        clearFields();
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
                        String categoryName = "";

                        if (t.getCategory() != null) {
                            categoryName = t.getCategory().getName();
                        }

                        matchesCategory = categoryName.toLowerCase()
                                .contains(keyword.toLowerCase());
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

    private boolean validateTransactionInput() {
        if (datePicker.getValue() == null) {
            showError("Please select a date.");
            return false;
        }

        if (datePicker.getValue().isAfter(LocalDate.now())) {
            showError("Future date is not allowed.");
            return false;
        }

        if (amountField.getText().trim().isEmpty()
                || categoryComboBox.getValue() == null
                || typeComboBox.getValue() == null) {
            showError("Fill all fields.");
            return false;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (Exception e) {
            showError("Invalid amount.");
            return false;
        }

        if (amount <= 0) {
            showError("Amount must be positive.");
            return false;
        }

        return true;
    }

    private int getSelectedCategoryId() {
        String categoryName = categoryComboBox.getValue();

        for (Category c : categories) {
            if (c.getName().equals(categoryName)) {
                return c.getId();
            }
        }

        return -1;
    }

    private void clearFields() {
        amountField.clear();
        categoryComboBox.setValue(null);
        typeComboBox.setValue(null);
        datePicker.setValue(null);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}