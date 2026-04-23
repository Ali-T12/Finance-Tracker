/**
 *علي حمال اسعيد 120220484  
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package controller;

import java.time.LocalDate;
import java.util.List;
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
import service.FileManager;
import util.AlertUtil;

/**
 *
 * @author Ali
 */
public class TransactionsController {

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

    private List<Category> categories;

    private int nextId = 1;

    @FXML
    public void initialize() {

        categories = FileManager.loadCategories();

        for (Category c : categories) {
            categoryComboBox.getItems().add(c.getName());
        }

        typeComboBox.setItems(FXCollections.observableArrayList("Income", "Expense"));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        categoryIdColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        List<Transaction> loadedTransactions = FileManager.loadTransactions();
        transactions.addAll(loadedTransactions);

        if (!loadedTransactions.isEmpty()) {
            int maxId = loadedTransactions.stream()
                    .mapToInt(Transaction::getId)
                    .max()
                    .getAsInt();
            nextId = maxId + 1;
        }

        transactionTable.setItems(transactions);

        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, selected) -> {
                    if (selected != null) {
                        amountField.setText(String.valueOf(selected.getAmount()));
                        typeComboBox.setValue(selected.getType());
                        datePicker.setValue(java.time.LocalDate.parse(selected.getDate()));

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

    @FXML
    private void handleAddTransaction() {
        
        if (datePicker.getValue() == null) {
    showError("Please select a date.");
    return;
}

if (datePicker.getValue().isAfter(java.time.LocalDate.now())) {
    showError("Future date is not allowed.");
    return;
}

        if (datePicker.getValue() == null) {
            AlertUtil.showError("Please select a date");
            return;
        }
        if (datePicker.getValue().isAfter(LocalDate.now())) {
            AlertUtil.showError("Future date is not allowed");
            return;
        }

        if (amountField.getText().isEmpty()
                || categoryComboBox.getValue() == null
                || typeComboBox.getValue() == null
                || datePicker.getValue() == null) {

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

        Transaction t = new Transaction(
                nextId++,
                1,
                categoryId,
                amount,
                typeComboBox.getValue(),
                datePicker.getValue().toString()
        );

        transactions.add(t);
        FileManager.saveTransactions(transactions);

        amountField.clear();
        categoryComboBox.setValue(null);
        typeComboBox.setValue(null);
        datePicker.setValue(null);
    }

    @FXML
    private void handleEditTransaction() {

        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        
        if (datePicker.getValue() == null) {
    showError("Please select a date.");
    return;
}

if (datePicker.getValue().isAfter(java.time.LocalDate.now())) {
    showError("Future date is not allowed.");
    return;
}

        if (selected == null) {
            showError("Select a transaction first.");
            return;
        }

        if (amountField.getText().isEmpty()
                || categoryComboBox.getValue() == null
                || typeComboBox.getValue() == null
                || datePicker.getValue() == null) {

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

        selected.setAmount(amount);
        selected.setType(typeComboBox.getValue());
        selected.setDate(datePicker.getValue().toString());
        selected.setCategoryId(categoryId);

        transactionTable.refresh();
        FileManager.saveTransactions(transactions);

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
