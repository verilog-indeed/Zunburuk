package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.DifferentialEquationType;
import dz.lightyearsoftworks.zunburuk.DifferentialSolver;
import dz.lightyearsoftworks.zunburuk.EquationParameters;
import dz.lightyearsoftworks.zunburuk.ODEDataPoint;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
    public LineChart graphChart;
    public Canvas mainCanvas;
    public DifferentialSolver currentSystem;
    private Timeline currentAnimation;

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

        double maxAngle, gravity, tetherLength;

        try {
            maxAngle = Double.parseDouble(maxMoveInputField.getText());
            gravity = Double.parseDouble(gravityInputField.getText());
            tetherLength = Double.parseDouble(lengthInputField.getText());
        } catch (NumberFormatException e)   {
            System.out.println("fix your regex, 7mar");
            return;
        }

        if (currentAnimation != null)   {
            currentAnimation.stop();
            graphChart.getData().clear();
        }

        XYChart.Series fxData = new XYChart.Series();
        graphChart.getData().add(fxData);

        mainCanvas.widthProperty().bind(((Pane)mainCanvas.getParent()).widthProperty());
        mainCanvas.heightProperty().bind(((Pane)mainCanvas.getParent()).heightProperty());


        currentSystem = new DifferentialSolver(DifferentialEquationType.ORDER2_PENDULUM,
                        new EquationParameters(maxAngle,
                            0,
                            gravity / tetherLength,
                            0, 0),
                    0.0, 1.0 / 60.0);

        updateChart = (dp, series) -> {
            series.getData().add(new XYChart.Data<>(dp.getT(), dp.getY()));
        };

        redrawSimulationObject = (dp) -> {
            GraphicsContext picassoThePainter = mainCanvas.getGraphicsContext2D();
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
            redrawCanvas();
            redrawSimulationObject.draw(dp);
            updateChart.update(dp, fxData);
        };

        currentAnimation = new Timeline(new KeyFrame(Duration.seconds(1/60.0), simulationSteppingHandler));
        currentAnimation.setCycleCount(Timeline.INDEFINITE);
        currentAnimation.play();
    }

    private void redrawCanvas() {
        GraphicsContext picassoThePainter = mainCanvas.getGraphicsContext2D();
        double width = mainCanvas.getWidth(); double height = mainCanvas.getHeight();
        picassoThePainter.clearRect(0, 0, width, height);
        //draws a nice lil rectangular base
        picassoThePainter.setFill(Color.GRAY);
        picassoThePainter.fillRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setLineWidth(2.0);
        picassoThePainter.setStroke(Color.BLACK);
        picassoThePainter.strokeRect(width/2.0 - 50.0, 40.0, 100.0, 20.0);
        picassoThePainter.setLineWidth(1.0);
    }

    SimulationSpecificRedraw redrawSimulationObject;
    SimulationSpecificChart updateChart;

    private interface SimulationSpecificChart {
        public abstract void update(ODEDataPoint currentState, XYChart.Series dataSeries);
    }

    private interface SimulationSpecificRedraw {
        public abstract void draw(ODEDataPoint currentState);
    }

}
