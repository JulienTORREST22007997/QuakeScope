package fr.julien.quakescope;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class EarthquakeMapApp extends Application {
    private static final String CSV_FILE_PATH = "path/to/your/csv/file.csv";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        Circle marker1 = createMarker(45.65, 0.15);
        Circle marker2 = createMarker(47.60, 3.22);
        Circle marker3 = createMarker(47.02, -0.10);

        root.getChildren().addAll(marker1, marker2, marker3);

        primaryStage.setTitle("Earthquake Map");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Circle createMarker(double latitude, double longitude) {
        double scaleFactor = 10; // Adjust this value to scale the markers according to your needs
        double x = (longitude + 180) * scaleFactor;
        double y = (90 - latitude) * scaleFactor;

        Circle marker = new Circle(x, y, 5, Color.RED); // Adjust the size and color of the marker as needed
        return marker;
    }
}
