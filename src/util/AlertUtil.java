/**
 *علي حمال اسعيد 120220484  
 * محمد منذر الغزالي  120220852
 * تحسين وسام عودة 120220463
 */
package util;

import javafx.scene.control.Alert;

/**
 *
 * @author Ali
 */
public class AlertUtil {
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
