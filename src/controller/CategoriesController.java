/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Category;
import service.FileManager;

/**
 *
 * @author Ali
 */
public class CategoriesController {
     @FXML
    private TextField nameField;

    @FXML
    private TableView<Category> categoryTable;

    @FXML
    private TableColumn<Category, Integer> idColumn;

    @FXML
    private TableColumn<Category, String> nameColumn;

    private ObservableList<Category> categories = FXCollections.observableArrayList();

    private int nextId = 1;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        List<Category> loadedCategories = FileManager.loadCategories();
        categories.addAll(loadedCategories);

        if (!loadedCategories.isEmpty()) {
            int maxId = loadedCategories.stream()
                    .mapToInt(Category::getId)
                    .max()
                    .getAsInt();
            nextId = maxId + 1;
        }

        categoryTable.setItems(categories);

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, selectedCategory) -> {
                    if (selectedCategory != null) {
                        nameField.setText(selectedCategory.getName());
                    }
                }
        );
    }

    @FXML
    private void handleAddCategory() {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showError("Category name is required.");
            return;
        }

        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                showError("Duplicate category name.");
                return;
            }
        }

        categories.add(new Category(nextId++, name));
        FileManager.saveCategories(categories);

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

        for (Category c : categories) {
            if (c != selected && c.getName().equalsIgnoreCase(newName)) {
                showError("Duplicate category name.");
                return;
            }
        }

        selected.setName(newName);
        categoryTable.refresh();
        FileManager.saveCategories(categories);

        nameField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
