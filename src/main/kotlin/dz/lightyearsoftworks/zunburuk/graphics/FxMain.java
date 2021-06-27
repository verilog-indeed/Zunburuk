package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    int currentDataIndex = 0;
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("main_stylesheet.fxml"));
        primaryStage.setTitle("Flamingo!");

        ArrayList<dataPoint> rawData = new DifferentialSolver()
                .solve(DifferentialEquationType.ORDER2_UNDAMPED,
                        new EquationParameters(1, 0, 1
                                                , 0, 0),
                        0.0, 6.28);


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
            fxData.getData().add(
                    new XYChart.Data<>(rawData.get(currentDataIndex).getT(), rawData.get(currentDataIndex).getY())
            );
            currentDataIndex++;
        };
        Timeline updateChart = new Timeline(new KeyFrame(Duration.millis(10), chartUpdater));
        updateChart.setCycleCount(Timeline.INDEFINITE);
        updateChart.play();
    }

    public static void main(String[] args)  {
        launch(args);
    }
}
