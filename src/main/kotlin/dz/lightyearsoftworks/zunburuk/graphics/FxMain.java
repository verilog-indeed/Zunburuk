package dz.lightyearsoftworks.zunburuk.graphics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;

public class FxMain extends Application {
    /*@Override
    public void start(Stage primaryStage) {
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
    }*/

    public static void main(String[] args)  {
        launch(args);
    }

    @Override
    public void start(Stage pStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_stylesheet.fxml"));
        loader.load();
        Parent root = loader.getRoot();
        /*
        * Perhaps no need to update everytime window is resized
        * if its going to update itself on every animation frame anyway?
        *
        canvas.widthProperty().bind(((Pane)canvas.getParent()).widthProperty());
        canvas.heightProperty().bind(((Pane)canvas.getParent()).heightProperty());
        canvas.widthProperty().addListener(evnt -> redrawCanvas());
        canvas.heightProperty().addListener(evnt -> redrawCanvas());*/

        Scene main = new Scene(root);
        pStage.setScene(main);
        pStage.show();
    }
}
