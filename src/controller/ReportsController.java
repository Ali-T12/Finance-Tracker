package controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CategorySummary;
import model.Transaction;
import repository.TransactionRepository;

public class ReportsController {

    @FXML private Label incomeLabel;
    @FXML private Label expenseLabel;
    @FXML private Label balanceLabel;
    @FXML private Label summaryLabel;

    @FXML private TableView<CategorySummary> categorySummaryTable;
    @FXML private TableColumn<CategorySummary, String> categoryNameColumn;
    @FXML private TableColumn<CategorySummary, Double> categoryTotalColumn;

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button generateButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private TextArea reportArea;

    private TransactionRepository transactionRepository = new TransactionRepository();
    private Task<String> reportTask;

    @FXML
    public void initialize() {
        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Monthly Income",
                "Monthly Expenses",
                "Category Spending",
                "Balance Summary"
        ));

        progressIndicator.setVisible(false);
        cancelButton.setDisable(true);

        loadReport();
    }

    private void loadReport() {
        List<Transaction> transactions = transactionRepository.findAll();

        if (transactions == null) {
            showError("Failed to load transactions for report.");
            return;
        }

        updateSummaryLabelsAndTable(transactions);
    }

    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeComboBox.getValue();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (reportType == null) {
            showError("Please select a report type.");
            return;
        }

        if (from == null || to == null) {
            showError("Please select from date and to date.");
            return;
        }

        if (from.isAfter(to)) {
            showError("Start date cannot be after end date.");
            return;
        }

        reportTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Loading transactions...");
                updateProgress(0, 1);

                List<Transaction> allTransactions = transactionRepository.findAll();

                List<Transaction> filtered = allTransactions.stream()
                        .filter(t -> {
                            LocalDate d = LocalDate.parse(t.getDate());
                            return !d.isBefore(from) && !d.isAfter(to);
                        })
                        .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    return "No data found for the selected report.";
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Report Type: ").append(reportType).append("\n");
                sb.append("From: ").append(from).append("\n");
                sb.append("To: ").append(to).append("\n");
                sb.append("========================\n\n");

                int total = filtered.size();

                double totalIncome = 0;
                double totalExpenses = 0;

                for (int i = 0; i < total; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    Transaction t = filtered.get(i);

                    boolean include = false;

                    if (reportType.equals("Monthly Income")) {
                        include = t.getType().equalsIgnoreCase("Income");
                    } else if (reportType.equals("Monthly Expenses")) {
                        include = t.getType().equalsIgnoreCase("Expense");
                    } else {
                        include = true;
                    }

                    if (include) {
                        sb.append("Category: ").append(t.getCategory().getName()).append("\n");
                        sb.append("Type: ").append(t.getType()).append("\n");
                        sb.append("Amount: ").append(t.getAmount()).append("\n");
                        sb.append("Date: ").append(t.getDate()).append("\n");
                        sb.append("------------------------\n");
                    }

                    if (t.getType().equalsIgnoreCase("Income")) {
                        totalIncome += t.getAmount();
                    } else if (t.getType().equalsIgnoreCase("Expense")) {
                        totalExpenses += t.getAmount();
                    }

                    updateProgress(i + 1, total);
                    updateMessage("Processed " + (i + 1) + "/" + total);
                    Thread.sleep(100);
                }

                if (reportType.equals("Category Spending")) {
                    sb.append("\nCategory Summary\n");
                    sb.append("========================\n");

                    Map<String, Double> categoryTotals = filtered.stream()
                            .collect(Collectors.groupingBy(
                                    t -> t.getCategory().getName(),
                                    Collectors.summingDouble(Transaction::getAmount)
                            ));

                    categoryTotals.forEach((category, amount) -> {
                        sb.append("Category: ").append(category).append("\n");
                        sb.append("Total Amount: ").append(amount).append("\n");
                        sb.append("------------------------\n");
                    });
                }

                double balance = totalIncome - totalExpenses;

                sb.append("\nFinal Totals\n");
                sb.append("Total Income: ").append(totalIncome).append("\n");
                sb.append("Total Expenses: ").append(totalExpenses).append("\n");
                sb.append("Balance: ").append(balance).append("\n");

                return sb.toString();
            }
        };

        progressIndicator.visibleProperty().bind(reportTask.runningProperty());
        generateButton.disableProperty().bind(reportTask.runningProperty());
        cancelButton.disableProperty().bind(reportTask.runningProperty().not());
        statusLabel.textProperty().bind(reportTask.messageProperty());

        reportTask.setOnSucceeded(e -> {
            reportArea.setText(reportTask.getValue());
            List<Transaction> transactions = transactionRepository.findAll();
            updateSummaryLabelsAndTable(transactions);
            unbindTaskControls();
            statusLabel.setText("Done");
        });

        reportTask.setOnCancelled(e -> {
            reportArea.setText("Report generation canceled.");
            unbindTaskControls();
            statusLabel.setText("Canceled");
            showInfo("Report generation canceled.");
        });

        reportTask.setOnFailed(e -> {
            unbindTaskControls();
            statusLabel.setText("Failed");
            showError("Failed to generate report.");
        });

        new Thread(reportTask, "report-generation-task").start();
    }

    @FXML
    private void handleCancelReport() {
        if (reportTask != null && reportTask.isRunning()) {
            reportTask.cancel();
        }
    }

    private void updateSummaryLabelsAndTable(List<Transaction> transactions) {
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

    private void unbindTaskControls() {
        progressIndicator.visibleProperty().unbind();
        progressIndicator.setVisible(false);

        generateButton.disableProperty().unbind();
        generateButton.setDisable(false);

        cancelButton.disableProperty().unbind();
        cancelButton.setDisable(true);

        statusLabel.textProperty().unbind();
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