package controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import service.AuthService;
import util.AlertUtil;
import util.Navigator;

/**
 *
 * @author Ali
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Please enter email and password.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            AlertUtil.showError("Invalid email format.");
            return;
        }

        User user = AuthService.findUserByEmail(email);

        if (user == null) {
            AlertUtil.showError("Account does not exist.");
            return;
        }

        if (!AuthService.checkPassword(user, password)) {
            AlertUtil.showError("Incorrect password.");
            return;
        }

        AlertUtil.showInfo("Login successful.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setWelcomeName(user.getFirstName());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Failed to load dashboard.");
        }
    }

    @FXML
    private void goToSignup() {
        Stage stage = (Stage) signupButton.getScene().getWindow();
        util.Navigator.switchScene(stage, "Signup.fxml", "Sign Up");
    }
}
