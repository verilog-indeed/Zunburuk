package dz.lightyearsoftworks.zunburuk.graphics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class FxMain extends Application {

    @Override
    public void start(Stage pStage) throws IOException {
        ResourceBundle bund = ResourceBundle.getBundle("dz.lightyearsoftworks.zunburuk.graphics.resources.strings", new Locale("ar"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main_stylesheet.fxml"), bund);
        loader.load();
        Parent root = loader.getRoot();
        pStage.setTitle("Zunburuk");
        pStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/zunburuk_icon.png")));
        Scene main = new Scene(root);
        pStage.setScene(main);
        pStage.show();
    }

    public static void main(String[] args)  {
        launch(args);
    }

}
