package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.DifferentialEquationType;
import dz.lightyearsoftworks.zunburuk.DifferentialSolver;
import dz.lightyearsoftworks.zunburuk.EquationParameters;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;

public class FxController {
    public VBox userSettings;
    public Button boi;
    public ComboBox oscillationTypeComboBox;
    public TextField gravityInputField;
    public TextField lengthInputField;
    public TextField maxMoveInputField;
    public Canvas mainCanvas;
    public DifferentialSolver currentSystem;

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

    public void onPlayButtonPress(ActionEvent actionEvent) {
        if (gravityInputField.getText().equals("") ||
            lengthInputField.getText().equals("") ||
            maxMoveInputField.getText().equals("")) {
            return;
        }

        mainCanvas.widthProperty().bind(((Pane)mainCanvas.getParent()).widthProperty());
        mainCanvas.heightProperty().bind(((Pane)mainCanvas.getParent()).heightProperty());

        try {
            currentSystem = new DifferentialSolver(DifferentialEquationType.ORDER2_PENDULUM,
                    new EquationParameters(Double.parseDouble(maxMoveInputField.getText()),
                            0,
                            Double.parseDouble(gravityInputField.getText()) / Double.parseDouble(lengthInputField.getText()),
                            0, 0),
                    0.0, 1.0 / 60.0);
        } catch (NumberFormatException e)   {
            System.out.println("fix your regex, 7mar");
            return;
        }

        redrawSimulationObject = () -> {

        };

        EventHandler<ActionEvent> simulationSteppingHandler = event -> {
            redrawCanvas();
            redrawSimulationObject.draw();
        };

        Timeline simulation = new Timeline(new KeyFrame(Duration.millis(1000.0/60.0), simulationSteppingHandler));
        simulation.setCycleCount(Timeline.INDEFINITE);
        simulation.play();
    }

    private void redrawCanvas() {
        GraphicsContext picassoThePainter = mainCanvas.getGraphicsContext2D();
        double width = mainCanvas.getWidth(); double height = mainCanvas.getHeight();
        picassoThePainter.clearRect(0, 0, width, height);
        //draws a nice lil rectangular base
        picassoThePainter.setFill(Color.GRAY);
        picassoThePainter.fillRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setStroke(Color.BLACK);
        picassoThePainter.strokeRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
    }

    SimulationSpecificRedraw redrawSimulationObject;
    private interface SimulationSpecificRedraw {
        public abstract void draw();
    }

}
