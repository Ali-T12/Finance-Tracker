


package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import util.DatabaseConnection;
import util.Session;

/**
 *
 * @author Ali
 */
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

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // جلب البيانات الخاصة بالمستخدم الحالي من قاعدة البيانات مباشرة
        loadCategoriesFromDatabase();

        sortComboBox.setItems(FXCollections.observableArrayList("Name Ascending", "Name Descending"));

        categoryTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, selectedCategory) -> {
                    if (selectedCategory != null) {
                        nameField.setText(selectedCategory.getName());
                    }
                }
        );
    }

    // دالة لجلب التصنيفات من قاعدة البيانات للمستخدم المسجل حالياً
    private void loadCategoriesFromDatabase() {
        categories.clear();
        String query = "SELECT * FROM categories WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, Session.currentUser.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load categories from database.");
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

        // التحقق من التكرار باستخدام Streams على القائمة المحملة
        boolean isDuplicate = categories.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));

        if (isDuplicate) {
            showError("Duplicate category name.");
            return;
        }

        // إدخال التصنيف الجديد في قاعدة البيانات
        String query = "INSERT INTO categories (user_id, name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, Session.currentUser.getId());
            ps.setString(2, name);
            ps.executeUpdate();

            // إعادة تحميل البيانات لتحديث الجدول بالـ ID الجديد التلقائي من قاعدة البيانات
            loadCategoriesFromDatabase();
            nameField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to add category to database.");
        }
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

        // التحقق من عدم تكرار الاسم مع أي تصنيف آخر لنفس المستخدم
        boolean isDuplicate = categories.stream()
                .anyMatch(c -> c.getId() != selected.getId() && c.getName().equalsIgnoreCase(newName));

        if (isDuplicate) {
            showError("Duplicate category name.");
            return;
        }

        // تحديث الاسم في قاعدة البيانات بناءً على الـ ID
        String query = "UPDATE categories SET name = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, newName);
            ps.setInt(2, selected.getId());
            ps.setInt(3, Session.currentUser.getId());
            ps.executeUpdate();

            // تحديث الواجهة مباشرة
            selected.setName(newName);
            categoryTable.refresh();
            nameField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to update category.");
        }
    }

    @FXML
    private void handleSearchCategory() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            categoryTable.setItems(categories);
            return;
        }

        // الفلترة باستخدام الـ Streams المطلوبة في المشروع
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