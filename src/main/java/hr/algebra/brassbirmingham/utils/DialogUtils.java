package hr.algebra.brassbirmingham.utils;

import hr.algebra.brassbirmingham.model.IndustryType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.util.Optional;

public class DialogUtils {
    private DialogUtils() {
    }

    public static void showAlertDialog(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static Optional<IndustryType> chooseIndustryDialog() {
        ChoiceDialog<IndustryType> dialog = new ChoiceDialog<>(IndustryType.COAL_MINE, IndustryType.values());
        dialog.setTitle("Izgradnja");
        dialog.setHeaderText(null);
        dialog.setContentText("Odaberite industriju za izgradnju:");
        return dialog.showAndWait();
    }

    public static boolean confirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

}
