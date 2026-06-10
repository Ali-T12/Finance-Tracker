package controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import model.Transaction;
import repository.CategoryRepository;
import repository.TransactionRepository;

public class TransactionsController {

    @FXML private TextField searchCategoryField;
    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker datePicker;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, Integer> idColumn;
    @FXML private TableColumn<Transaction, Integer> userIdColumn;
    @FXML private TableColumn<Transaction, String> categoryIdColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;

    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private List<Category> categories = new ArrayList<>();

    private CategoryRepository categoryRepository = new CategoryRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        userIdColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getUser().getId())
        );

        categoryIdColumn.setCellValueFactory(c ->
                new ReadOnlyObjectWrapper<>(c.getValue().getCategory().getName())
        );

        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        typeComboBox.setItems(FXCollections.observableArrayList("Income", "Expense"));
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Amount Ascending",
                "Amount Descending",
                "Date Ascending",
                "Date Descending"
        ));

        loadCategories();
        loadTransactions();

        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, selected) -> {
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

    // ================= LOAD CATEGORIES =================
    private void loadCategories() {

        new Thread(() -> {

            List<Category> list = categoryRepository.findAllSync();

            Platform.runLater(() -> {
                categories.clear();
                categoryComboBox.getItems().clear();

                if (list != null) {
                    categories.addAll(list);

                    for (Category c : list) {
                        categoryComboBox.getItems().add(c.getName());
                    }
                }
            });

        }).start();
    }

    // ================= LOAD TRANSACTIONS =================
    private void loadTransactions() {

        new Thread(() -> {

            List<Transaction> list = transactionRepository.findAll();

            Platform.runLater(() -> {
                transactions.clear();

                if (list != null) {
                    transactions.addAll(list);
                }

                transactionTable.setItems(transactions);
            });

        }).start();
    }

    // ================= ADD =================
    @FXML
    private void handleAddTransaction() {

        if (!validateTransactionInput()) return;

        double amount = Double.parseDouble(amountField.getText().trim());
        int categoryId = getSelectedCategoryId();

        if (categoryId == -1) {
            showError("Selected category not found.");
            return;
        }

        transactionRepository.addAsync(
                categoryId,
                amount,
                typeComboBox.getValue(),
                datePicker.getValue().toString()
        );

        clearFields();
        loadTransactions();
    }

    // ================= EDIT =================
    @FXML
    private void handleEditTransaction() {

        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select a transaction first.");
            return;
        }

        if (!validateTransactionInput()) return;

        double amount = Double.parseDouble(amountField.getText().trim());
        int categoryId = getSelectedCategoryId();

        if (categoryId == -1) {
            showError("Selected category not found.");
            return;
        }

        transactionRepository.updateAsync(
                selected.getId(),
                categoryId,
                amount,
                typeComboBox.getValue(),
                datePicker.getValue().toString()
        );

        clearFields();
        loadTransactions();
    }

    // ================= SEARCH =================
    @FXML
    private void handleSearchTransaction() {

        String keyword = searchCategoryField.getText().trim();
        LocalDate date = filterDatePicker.getValue();

        if (keyword.isEmpty() && date == null) {
            showError("Enter search data.");
            return;
        }

        List<Transaction> result = transactions.stream()
                .filter(t -> {
                    boolean okCategory = true;
                    boolean okDate = true;

                    if (!keyword.isEmpty()) {
                        String name = (t.getCategory() != null)
                                ? t.getCategory().getName()
                                : "";

                        okCategory = name.toLowerCase().contains(keyword.toLowerCase());
                    }

                    if (date != null) {
                        okDate = t.getDate().equals(date.toString());
                    }

                    return okCategory && okDate;
                })
                .collect(Collectors.toList());

        transactionTable.setItems(FXCollections.observableArrayList(result));
    }

    // ================= SORT =================
    @FXML
    private void handleSortTransaction() {

        String option = sortComboBox.getValue();

        if (option == null) {
            showError("Select sort option.");
            return;
        }

        List<Transaction> sorted;

        switch (option) {

            case "Amount Ascending":
                sorted = transactions.stream()
                        .sorted(Comparator.comparingDouble(Transaction::getAmount))
                        .collect(Collectors.toList());
                break;

            case "Amount Descending":
                sorted = transactions.stream()
                        .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                        .collect(Collectors.toList());
                break;

            case "Date Ascending":
                sorted = transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getDate))
                        .collect(Collectors.toList());
                break;

            default:
                sorted = transactions.stream()
                        .sorted(Comparator.comparing(Transaction::getDate).reversed())
                        .collect(Collectors.toList());
                break;
        }

        transactionTable.setItems(FXCollections.observableArrayList(sorted));
    }

    // ================= RESET =================
    @FXML
    private void handleResetTransaction() {
        transactionTable.setItems(transactions);
        searchCategoryField.clear();
        filterDatePicker.setValue(null);
        sortComboBox.setValue(null);
    }

    // ================= VALIDATION =================
    private boolean validateTransactionInput() {

        if (datePicker.getValue() == null) {
            showError("Select date.");
            return false;
        }

        if (datePicker.getValue().isAfter(LocalDate.now())) {
            showError("Future date not allowed.");
            return false;
        }

        if (amountField.getText().trim().isEmpty()
                || categoryComboBox.getValue() == null
                || typeComboBox.getValue() == null) {
            showError("Fill all fields.");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showError("Amount must be positive.");
                return false;
            }
        } catch (Exception e) {
            showError("Invalid amount.");
            return false;
        }

        return true;
    }

    // ================= CATEGORY ID =================
    private int getSelectedCategoryId() {

        String name = categoryComboBox.getValue();

        for (Category c : categories) {
            if (c.getName().equals(name)) {
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