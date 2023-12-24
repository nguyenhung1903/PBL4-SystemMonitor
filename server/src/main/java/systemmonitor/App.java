package systemmonitor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import systemmonitor.Controllers.OverviewController;
import systemmonitor.Server.Server;
import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    // Launch() method will invoke this function
    @Override
    public void start(Stage stage) throws IOException {
        // Start server to communicate with clients
        Server server = new Server();
        // app.LoadServerConfig("src\\main\\resources\\config\\config.cfg");

        // set UI
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("overview" + ".fxml"));
        scene = new Scene((Parent) fxmlLoader.load(), 525, 520);
        stage.setScene(scene);
        stage.setTitle("System Monitor");
        OverviewController overviewController = fxmlLoader.getController();
        server.setController(overviewController);
        server.start();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // public static FXMLLoader getLoader(String fxml) throws IOException {
    // return new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    // }

    public static void main(String[] args) {
        launch();
    }
}