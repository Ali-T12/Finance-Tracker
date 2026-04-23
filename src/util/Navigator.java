/**
 *علي حمال اسعيد 120220484  
 * محمد منذر الغزالي 120220852
 * تحسين وسام عودة 120220463
 */
package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Ali
 */
public class Navigator {

    public static void switchScene(Stage stage, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/view/" + fxmlFile));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
