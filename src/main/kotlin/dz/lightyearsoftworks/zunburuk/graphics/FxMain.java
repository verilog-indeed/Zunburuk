package dz.lightyearsoftworks.zunburuk.graphics;

import dz.lightyearsoftworks.zunburuk.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class FxMain extends Application {
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
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (dataPoint dp: rawData) {
                    Platform.runLater(() ->
                    {
                        fxData.getData().add(new XYChart.Data(dp.getT(), dp.getY()));
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public static void main(String[] args)  {
        launch(args);
    }
}
