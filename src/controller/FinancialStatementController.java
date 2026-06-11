package controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Category;
import model.Transaction;
import repository.CategoryRepository;
import repository.TransactionRepository;

public class FinancialStatementController {

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Button generateButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea statementArea;

    private Task<String> statementTask;

    private TransactionRepository transactionRepository = new TransactionRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();

    private List<Category> categories;

    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        cancelButton.setDisable(true);

        categories = categoryRepository.findAllSync();

        if (categories != null) {
            for (Category c : categories) {
                categoryComboBox.getItems().add(c.getName());
            }
        }
    }

    @FXML
    private void handleGenerateStatement() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String selectedCategory = categoryComboBox.getValue();

        if (from == null || to == null) {
            showError("Please select from date and to date.");
            return;
        }

        if (from.isAfter(to)) {
            showError("Start date cannot be after end date.");
            return;
        }

        statementTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Loading transactions...");
                updateProgress(0, 1);

                List<Transaction> transactions = transactionRepository.findAll();

                if (transactions == null || transactions.isEmpty()) {
                    return "No transactions found for the selected range.";
                }

                List<Transaction> filtered = transactions.stream()
                        .filter(t -> {
                            LocalDate transactionDate = LocalDate.parse(t.getDate());
                            return !transactionDate.isBefore(from) && !transactionDate.isAfter(to);
                        })
                        .filter(t -> {
                            if (selectedCategory == null || selectedCategory.isEmpty()) {
                                return true;
                            }
                            return t.getCategory().getName().equalsIgnoreCase(selectedCategory);
                        })
                        .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    return "No transactions found for the selected range.";
                }

                double totalIncome = 0;
                double totalExpenses = 0;

                StringBuilder report = new StringBuilder();
                report.append("Financial Statement\n");
                report.append("====================\n");
                report.append("From: ").append(from).append("\n");
                report.append("To: ").append(to).append("\n\n");

                int total = filtered.size();

                for (int i = 0; i < total; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    Transaction t = filtered.get(i);

                    report.append("Type: ").append(t.getType()).append("\n");
                    report.append("Category: ").append(t.getCategory().getName()).append("\n");
                    report.append("Amount: ").append(t.getAmount()).append("\n");
                    report.append("Date: ").append(t.getDate()).append("\n");
                    report.append("--------------------\n");

                    if (t.getType().equalsIgnoreCase("Income")) {
                        totalIncome += t.getAmount();
                    } else if (t.getType().equalsIgnoreCase("Expense")) {
                        totalExpenses += t.getAmount();
                    }

                    updateProgress(i + 1, total);
                    updateMessage("Processed " + (i + 1) + "/" + total);
                    Thread.sleep(100);
                }

                double balance = totalIncome - totalExpenses;

                report.append("\nFinal Totals\n");
                report.append("Total Income: ").append(totalIncome).append("\n");
                report.append("Total Expenses: ").append(totalExpenses).append("\n");
                report.append("Balance: ").append(balance).append("\n");

                return report.toString();
            }
        };

        progressIndicator.visibleProperty().bind(statementTask.runningProperty());
        generateButton.disableProperty().bind(statementTask.runningProperty());
        cancelButton.disableProperty().bind(statementTask.runningProperty().not());
        statusLabel.textProperty().bind(statementTask.messageProperty());

        statementTask.setOnSucceeded(e -> {
            statementArea.setText(statementTask.getValue());
            statusLabel.textProperty().unbind();
            statusLabel.setText("Done");
            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);
        });

        statementTask.setOnCancelled(e -> {
            statementArea.setText("Statement generation canceled.");
            statusLabel.textProperty().unbind();
            statusLabel.setText("Canceled");
            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);
            showInfo("Statement generation canceled.");
        });

        statementTask.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            statusLabel.setText("Failed");
            progressIndicator.visibleProperty().unbind();
            progressIndicator.setVisible(false);
            showError("Failed to generate statement.");
        });

        new Thread(statementTask, "financial-statement-task").start();
    }

    @FXML
    private void handleCancelStatement() {
        if (statementTask != null && statementTask.isRunning()) {
            statementTask.cancel();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}