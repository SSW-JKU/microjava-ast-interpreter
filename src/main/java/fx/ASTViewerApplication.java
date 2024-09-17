package fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ASTViewerApplication extends Application {

    FXMLLoader fxmlLoader;
    @Override
    public void start(Stage stage) throws IOException {
        this.fxmlLoader = new FXMLLoader(ASTViewerApplication.class.getResource("ast-view.fxml"));
        stage.getIcons().add(new Image(Objects.requireNonNull(ASTViewerController.class.getResourceAsStream("/jku.png"))));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}