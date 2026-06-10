package controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import repository.CategoryRepository;

public class CategoriesController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private TextField nameField;

    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private TableColumn<Category, Integer> idColumn;

    @FXML
    private TableColumn<Category, String> nameColumn;

    private ObservableList<Category> categories = FXCollections.observableArrayList();

    private CategoryRepository categoryRepository = new CategoryRepository();

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        sortComboBox.setItems(
                FXCollections.observableArrayList("Name Ascending", "Name Descending")
        );

        loadCategories();

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, selectedCategory) -> {
                    if (selectedCategory != null) {
                        nameField.setText(selectedCategory.getName());
                    }
                }
        );
    }

    // ================= LOAD =================
   private void loadCategories() {

    new Thread(() -> {

        List<Category> list = categoryRepository.findAllSync(); // مهم تكون Sync

        Platform.runLater(() -> {
            categories.clear();

            if (list != null) {
                categories.addAll(list);
            }

            categoryTable.setItems(categories);
        });

    }).start();
}

    // ================= ADD =================
    @FXML
    private void handleAddCategory() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showError("Category name is required.");
            return;
        }

        boolean isDuplicate = categories.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));

        if (isDuplicate) {
            showError("Duplicate category name.");
            return;
        }

        categoryRepository.addAsync(name);

        nameField.clear();

        // reload بعد شوي لأن العملية async
        refreshAfterDelay();
    }

    // ================= EDIT =================
    @FXML
    private void handleEditCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select category first.");
            return;
        }

        String newName = nameField.getText().trim();

        if (newName.isEmpty()) {
            showError("Category name is required.");
            return;
        }

        boolean isDuplicate = categories.stream()
                .anyMatch(c -> c.getId() != selected.getId()
                        && c.getName().equalsIgnoreCase(newName));

        if (isDuplicate) {
            showError("Duplicate category name.");
            return;
        }

        selected.setName(newName);

        categoryRepository.updateAsync(selected);

        nameField.clear();
        refreshAfterDelay();
    }

    // ================= SEARCH =================
    @FXML
    private void handleSearchCategory() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            categoryTable.setItems(categories);
            return;
        }

        List<Category> result = categories.stream()
                .filter(c -> c.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            showError("No categories found.");
            return;
        }

        categoryTable.setItems(FXCollections.observableArrayList(result));
    }

    // ================= SORT =================
    @FXML
    private void handleSortCategory() {
        String sortOption = sortComboBox.getValue();

        if (sortOption == null) {
            showError("Please select a sorting option.");
            return;
        }

        List<Category> sortedList;

        if (sortOption.equals("Name Ascending")) {
            sortedList = categories.stream()
                    .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        } else {
            sortedList = categories.stream()
                    .sorted(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER).reversed())
                    .collect(Collectors.toList());
        }

        categoryTable.setItems(FXCollections.observableArrayList(sortedList));
    }

    // ================= RESET =================
    @FXML
    private void handleResetCategory() {
        categoryTable.setItems(categories);
        searchField.clear();
        sortComboBox.setValue(null);
    }

    // ================= REFRESH =================
    private void refreshAfterDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(300); // small delay عشان DB تخلص

                List<Category> list = categoryRepository.findAllSync();

                Platform.runLater(() -> {
                    if (list != null) {
                        categories.setAll(list);
                        categoryTable.setItems(categories);
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ================= ERROR =================
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}