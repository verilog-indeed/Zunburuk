package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.DifferentialEquationType;
import dz.lightyearsoftworks.zunburuk.DifferentialSolver;
import dz.lightyearsoftworks.zunburuk.EquationParameters;
import dz.lightyearsoftworks.zunburuk.ODEDataPoint;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FxController implements Initializable {
    public VBox userSettings;
    public ComboBox<String> oscillationTypeComboBox;
    public TextField gravityInputField;
    public TextField lengthInputField;
    public TextField maxAngleInputField;
    public TextField springConstInputField;
    public TextField massInputField;
    public TextField maxDisplacementInputField;
    public Canvas mainCanvas;
    public DifferentialSolver currentSystem;
    public ImageView imgEqn;
    public Button playButton;
    private GraphicsContext picassoThePainter;
    private Timeline currentAnimation;
    private final ArrayList<TextField> availableInputFields = new ArrayList<>();
    SimulationSpecificRedraw redrawSimulationObject;


    public void onPlayButtonPress(ActionEvent actionEvent) {
        for (TextField t: availableInputFields) {
            if (t.getText().equals("")) {
                return;
            }
        }


        double maxAngle, gravity, tetherLength;

        try {
            maxAngle = Double.parseDouble(maxAngleInputField.getText());
            gravity = Double.parseDouble(gravityInputField.getText());
            tetherLength = Double.parseDouble(lengthInputField.getText());
        } catch (NumberFormatException e)   {
            System.out.println("fix your regex, 7mar");
            return;
        }

        if (currentAnimation != null)   {
            currentAnimation.stop();
        }

        mainCanvas.widthProperty().bind(((Pane)mainCanvas.getParent()).widthProperty());
        mainCanvas.heightProperty().bind(((Pane)mainCanvas.getParent()).heightProperty());
        picassoThePainter = mainCanvas.getGraphicsContext2D();

        currentSystem = new DifferentialSolver(DifferentialEquationType.ORDER2_PENDULUM,
                        new EquationParameters(maxAngle,
                            0,
                            gravity / tetherLength,
                            0, 0),
                    0.0, 1.0 / 60.0);

        redrawSimulationObject = (dp) -> {
            double width = mainCanvas.getWidth(); double height = mainCanvas.getHeight();
            picassoThePainter.setLineWidth(3.0);
            double currentX = width/2.0 + 100 * tetherLength * Math.sin(dp.getY());
            double currentY = 60 + 100 * tetherLength * Math.cos(dp.getY());
            picassoThePainter.strokeLine(width/2.0,
                                        60,
                                        currentX,
                                        currentY);
            picassoThePainter.setFill(Color.INDIANRED);
            picassoThePainter.fillOval(currentX - 15.0,currentY - 15.0,30.0,30.0);
            picassoThePainter.strokeOval(currentX - 15.0,currentY - 15.0,30.0,30.0);
            picassoThePainter.setLineWidth(1.0);
        };

        EventHandler<ActionEvent> simulationSteppingHandler = event -> {
            ODEDataPoint dp = currentSystem.nextDataPoint();
            clearCanvas();
            redrawSimulationObject.draw(dp);
            redrawBase();
        };

        currentAnimation = new Timeline(new KeyFrame(Duration.seconds(1/60.0), simulationSteppingHandler));
        currentAnimation.setCycleCount(Timeline.INDEFINITE);
        currentAnimation.play();
    }

    private void redrawBase() {
        //draws a nice lil rectangular base
        double width = mainCanvas.getWidth(); double height = mainCanvas.getHeight();
        picassoThePainter.setFill(Color.GRAY);
        picassoThePainter.fillRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setLineWidth(2.0);
        picassoThePainter.setStroke(Color.BLACK);
        picassoThePainter.strokeRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setLineWidth(1.0);
    }

    private void clearCanvas() {
        double width = mainCanvas.getWidth(); double height = mainCanvas.getHeight();
        picassoThePainter.clearRect(0, 0, width, height);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        oscillationTypeComboBox.getItems().add("Simple Pendulum");
        oscillationTypeComboBox.getItems().add("Mass on a Vertical Spring");
    }

    public void onSimTypeSelection(ActionEvent actionEvent) {
        for (TextField t: availableInputFields)  {
            t.clear();
            t.setManaged(false);
            t.setVisible(false);
        }
        availableInputFields.clear();
        switch (oscillationTypeComboBox.getValue()) {
            case ("Simple Pendulum"):
                availableInputFields.add(gravityInputField);
                availableInputFields.add(lengthInputField);
                availableInputFields.add(maxAngleInputField);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_pendulum.png")));
                break;
            case ("Mass on a Vertical Spring"):
                availableInputFields.add(springConstInputField);
                availableInputFields.add(massInputField);
                availableInputFields.add(maxDisplacementInputField);
                imgEqn.setImage(new Image(getClass().getResourceAsStream("resources/ode_springmass.png")));
                break;
            default:
        }
        imgEqn.setManaged(true);
        imgEqn.setVisible(true);

        playButton.setManaged(true);
        playButton.setVisible(true);
        for(TextField t: availableInputFields)  {
            t.setManaged(true);
            t.setVisible(true);
        }
    }

    public void onInputFieldChanged(KeyEvent actionEvent) {
        TextField source = (TextField) actionEvent.getSource();

        /*Make sure the pattern matches a decimal number, if not it just clears the text field
         *everytime the user types an invalid pattern
         * */

        if (!source.getText().matches("((\\d+)(\\.)?(\\d+)?){1}")) {
            source.clear();
        }
    }

    private interface SimulationSpecificRedraw {
        public abstract void draw(ODEDataPoint currentState);
    }
}
