/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import util.Navigator;

/**
 *
 * @author Ali
 */
public class DashboardController {

    @FXML
    private Button categoriesButton;

    @FXML
    private Button transactionsButton;

    @FXML
    private Button reportsButton;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label lblWelcome;

    public void setWelcomeName(String firstName) {
        lblWelcome.setText("Welcome, " + firstName + "!");
    }

    @FXML
    private void goToCategories() {
        loadPage("Categories.fxml");
    }

    @FXML
    private void goToTransactions() {
        loadPage("Transactions.fxml");
    }

    @FXML
    private void goToReports() {
        loadPage("Reports.fxml");
    }

    private void loadPage(String page) {
        try {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(
                    FXMLLoader.load(getClass().getResource("/view/" + page))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button logoutButton;

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        util.Navigator.switchScene(stage, "Login.fxml", "Login");
        stage.setMaximized(false);
    }

}