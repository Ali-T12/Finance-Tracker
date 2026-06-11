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
import javafx.concurrent.Task;
import java.util.Map;

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

    @FXML
    private Button refreshButton;
    @FXML
    private Button summaryButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea summaryArea;

    private Task<?> currentTask;
  
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private List<Category> categories = new ArrayList<>();

    private CategoryRepository categoryRepository = new CategoryRepository();
    private TransactionRepository transactionRepository = new TransactionRepository();

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        userIdColumn.setCellValueFactory(c
                -> new ReadOnlyObjectWrapper<>(c.getValue().getUser().getId())
        );

        categoryIdColumn.setCellValueFactory(c
                -> new ReadOnlyObjectWrapper<>(c.getValue().getCategory().getName())
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
         progressIndicator.setVisible(false);
        cancelButton.setDisable(true);

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
        
        summaryArea.setVisible(false);
       
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

    Task<List<Transaction>> task = new Task<List<Transaction>>() {
        @Override
        protected List<Transaction> call() throws Exception {
            updateMessage("Loading transactions...");
            List<Transaction> list = transactionRepository.findAll();

            if (list == null) {
                return new ArrayList<>();
            }

            int total = list.size();

            for (int i = 0; i < total; i++) {
                if (isCancelled()) {
                    break;
                }

                updateProgress(i + 1, total);
                updateMessage("Loaded " + (i + 1) + "/" + total);
                Thread.sleep(50);
            }

            return list;
        }
    };

    currentTask = task;

    bindTask(task);

    task.setOnSucceeded(e -> {
        transactions.clear();
        transactions.addAll(task.getValue());
        transactionTable.setItems(transactions);
        unbindTask();
        statusLabel.setText("Done");
    });

    task.setOnCancelled(e -> {
        unbindTask();
        statusLabel.setText("Canceled");
        showInfo("Loading canceled.");
    });

    task.setOnFailed(e -> {
        unbindTask();
        statusLabel.setText("Failed");
        showError("Failed to load transactions.");
    });

    new Thread(task, "transactions-refresh-task").start();
}
   private void bindTask(Task<?> task) {
    progressIndicator.visibleProperty().unbind();
    progressIndicator.progressProperty().unbind();
    refreshButton.disableProperty().unbind();
    summaryButton.disableProperty().unbind();
    cancelButton.disableProperty().unbind();
    statusLabel.textProperty().unbind();

    progressIndicator.visibleProperty().bind(task.runningProperty());
    progressIndicator.progressProperty().bind(task.progressProperty());

    refreshButton.disableProperty().bind(task.runningProperty());
    summaryButton.disableProperty().bind(task.runningProperty());
    cancelButton.disableProperty().bind(task.runningProperty().not());

    statusLabel.textProperty().bind(task.messageProperty());
}
   
   private void unbindTask() {
    progressIndicator.visibleProperty().unbind();
    progressIndicator.progressProperty().unbind();
    progressIndicator.setVisible(false);
    progressIndicator.setProgress(0);

    refreshButton.disableProperty().unbind();
    refreshButton.setDisable(false);

    summaryButton.disableProperty().unbind();
    summaryButton.setDisable(false);

    cancelButton.disableProperty().unbind();
    cancelButton.setDisable(true);

    statusLabel.textProperty().unbind();
}
   
   private void showInfo(String msg) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(msg);
    alert.showAndWait();
}
   
   @FXML
private void handleRefreshTransactions() {
    loadTransactions();
}

@FXML
private void handleGenerateCategorySummary() {

    Task<String> task = new Task<String>() {
        @Override
        protected String call() throws Exception {
            updateMessage("Generating category summary...");

            List<Transaction> list = transactionRepository.findAll();

            if (list == null || list.isEmpty()) {
                return "No transactions found.";
            }

            Map<String, Double> summary = list.stream()
                    .filter(t -> t.getCategory() != null)
                    .collect(Collectors.groupingBy(
                            t -> t.getCategory().getName(),
                            Collectors.summingDouble(Transaction::getAmount)
                    ));

            StringBuilder sb = new StringBuilder();
            sb.append("Category Summary\n");
            sb.append("====================\n");

            int total = summary.size();
            int count = 0;

            for (Map.Entry<String, Double> entry : summary.entrySet()) {
                if (isCancelled()) {
                    break;
                }

                sb.append("Category: ").append(entry.getKey()).append("\n");
                sb.append("Total Amount: ").append(entry.getValue()).append("\n");
                sb.append("--------------------\n");

                count++;
                updateProgress(count, total);
                updateMessage("Processed " + count + "/" + total);
                Thread.sleep(100);
            }

            return sb.toString();
        }
    };

    currentTask = task;

    bindTask(task);

    task.setOnSucceeded(e -> {
        summaryArea.setText(task.getValue());
        unbindTask();
        statusLabel.setText("Done");
    });

    task.setOnCancelled(e -> {
        summaryArea.setText("Category summary canceled.");
        unbindTask();
        statusLabel.setText("Canceled");
        showInfo("Category summary canceled.");
    });

    task.setOnFailed(e -> {
        unbindTask();
        statusLabel.setText("Failed");
        showError("Failed to generate category summary.");
    });

    new Thread(task, "category-summary-task").start();
}

@FXML
private void handleCancelTask() {
    if (currentTask != null && currentTask.isRunning()) {
        currentTask.cancel();
    }
}

    // ================= ADD =================
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

        if (!validateTransactionInput()) {
            return;
        }

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
    
    @FXML
private void showTransactions() {

    transactionTable.setVisible(true);
    summaryArea.setVisible(false);
}

@FXML
private void showSummary() {

    transactionTable.setVisible(false);
    summaryArea.setVisible(true);
}
}
