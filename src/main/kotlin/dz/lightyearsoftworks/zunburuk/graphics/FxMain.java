package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class FxMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("main_stylesheet.fxml"));
        primaryStage.setTitle("Flamingo!");

        DifferentialSolver oscillatorSystem = new DifferentialSolver(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(1, 0, 1
                                                , 0, 0),
                        0.0, 0.017);


        NumberAxis timeAxis = new NumberAxis();
        timeAxis.setLabel("Time");

        NumberAxis motionAxis = new NumberAxis();
        motionAxis.setLabel("Y(t)");

        LineChart graph = new LineChart(timeAxis, motionAxis);
        XYChart.Series fxData = new XYChart.Series();
        fxData.setName("cos(t)");

        graph.getData().add(fxData);

        VBox vbox = new VBox(graph);

        Scene first = new Scene(vbox,640, 480, Color.LIGHTPINK);
        primaryStage.setScene(first);
        primaryStage.show();

        EventHandler<ActionEvent> chartUpdater = event -> {
            ODEDataPoint nextValue = oscillatorSystem.nextDataPoint();
            fxData.getData().add(
                    new XYChart.Data<>(nextValue.getT(), nextValue.getY())
            );
        };
        Timeline updateChart = new Timeline(new KeyFrame(Duration.millis(17), chartUpdater));
        updateChart.setCycleCount(Timeline.INDEFINITE);
        updateChart.play();
    }

    public static void main(String[] args)  {
        launch(args);
    }
}
