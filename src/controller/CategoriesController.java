/**
 * علي حمال اسعيد 120220484
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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

        loadCategories();

        sortComboBox.setItems(
                FXCollections.observableArrayList("Name Ascending", "Name Descending")
        );

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, selectedCategory) -> {
                    if (selectedCategory != null) {
                        nameField.setText(selectedCategory.getName());
                    }
                }
        );
    }

    private void loadCategories() {
        categories.clear();
        List<Category> categoryList = categoryRepository.findAll();

        if (categoryList != null) {
            categories.addAll(categoryList);
        }

        categoryTable.setItems(categories);
    }

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

        Category addedCategory = categoryRepository.add(name);

        if (addedCategory == null) {
            showError("Failed to add category.");
            return;
        }

        loadCategories();
        nameField.clear();
    }

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
        Category updatedCategory = categoryRepository.update(selected);

        if (updatedCategory == null) {
            showError("Failed to update category.");
            return;
        }

        loadCategories();
        nameField.clear();
    }

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

    @FXML
    private void handleResetCategory() {
        categoryTable.setItems(categories);
        searchField.clear();
        sortComboBox.setValue(null);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
