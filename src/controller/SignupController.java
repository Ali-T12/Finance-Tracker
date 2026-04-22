package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthService;
import util.AlertUtil;
import util.Navigator;

public class SignupController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Button cancelButton;

    @FXML
    private void handleSignup() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            AlertUtil.showError("Please fill all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertUtil.showError("Passwords do not match.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AlertUtil.showError("Please enter a valid email address.");
            return;
        }

        boolean success = AuthService.signUp(
                1,
                firstName,
                lastName,
                email,
                password
        );

        if (success) {
            AlertUtil.showInfo("Signup successful.");
            Stage stage = (Stage) signupButton.getScene().getWindow();
            Navigator.switchScene(stage, "Login.fxml", "Login");
        } else {
            AlertUtil.showError("Email already exists.");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        Navigator.switchScene(stage, "Login.fxml", "Login");
    }
}
