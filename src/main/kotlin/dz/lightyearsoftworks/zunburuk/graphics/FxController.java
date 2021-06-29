package dz.lightyearsoftworks.zunburuk.graphics;

import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class FxController {
    public VBox userSettings;
    public Button boi;
    public ComboBox oscillationTypeComboBox;
    public TextField gravityInputField;
    public TextField lengthInputField;
    public TextField maxMoveInputField;
    public Canvas mainCanvas;

    public void onInputFieldChanged(KeyEvent actionEvent) {
        TextField source = (TextField) actionEvent.getSource();
        /*
        *checks if there is text which doesn't match the "decimal number"
        *pattern, replaces violating characters with nothing
        * */
        if (!source.getText().matches("(\\d+)(\\.)?(\\d+)?")) {
            source.setText(source.getText().replaceAll("[^((\\d+)(\\.)?(\\d+)?)]", ""));
            source.positionCaret(source.getText().length()); //returns caret to the end of the field
        }
    }
}
