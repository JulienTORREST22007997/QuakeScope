package fr.julien.quakescope;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class EarthquakeDashboardApp extends Application {
    private static final String CHEMIN_FICHIER_CSV = "/amuhome/t22007997/Téléchargements/QuakeScope/src/main/resources/fr/julien/quakescope/file.csv";

    private ObservableList<DonneesSeisme> donnees = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        chargerDonneesDepuisCSV(CHEMIN_FICHIER_CSV);

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        Slider filtreMagnitudeSlider = new Slider(0, 10, 5);
        filtreMagnitudeSlider.setShowTickLabels(true);
        filtreMagnitudeSlider.setShowTickMarks(true);
        filtreMagnitudeSlider.setMajorTickUnit(1);
        filtreMagnitudeSlider.setMinorTickCount(0);
        filtreMagnitudeSlider.setBlockIncrement(1);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(filtreMagnitudeSlider);

        root.setTop(vbox);

        TabPane tabPane = new TabPane();
        root.setCenter(tabPane);

        Tab graphiqueTab = new Tab("Graphique");
        graphiqueTab.setContent(creerGraphique());

        Tab tableauTab = new Tab("Tableau");
        tableauTab.setContent(creerTableau(filtreMagnitudeSlider));

        Tab regionTab = new Tab("Régions");
        regionTab.setContent(creerGraphiqueRegions());

        tabPane.getTabs().addAll(graphiqueTab, tableauTab, regionTab);

        primaryStage.setTitle("Tableau de bord des séismes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chargerDonneesDepuisCSV(String cheminFichier) {
        try (Scanner scanner = new Scanner(new File(cheminFichier))) {
            // Ignorer la première ligne
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String ligne = scanner.nextLine();
                String[] colonnes = ligne.split(",");

                if (colonnes.length >= 11) {
                    String region = colonnes[4].trim();
                    String latitudeString = colonnes[8].trim();
                    String longitudeString = colonnes[9].trim();
                    String magnitudeString = colonnes[10].trim();

                    if (!region.isEmpty() && !latitudeString.isEmpty() && !longitudeString.isEmpty() && !magnitudeString.isEmpty()) {
                        try {
                            double latitude = Double.parseDouble(latitudeString);
                            double longitude = Double.parseDouble(longitudeString);
                            double magnitude = Double.parseDouble(magnitudeString);
                            DonneesSeisme donneesSeisme = new DonneesSeisme(region, latitude, longitude, magnitude);
                            donnees.add(donneesSeisme);
                        } catch (NumberFormatException e) {
                            System.err.println("Valeur numérique invalide dans la ligne CSV : " + ligne);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private PieChart creerGraphique() {
        PieChart chart = new PieChart();
        chart.setData(getDonneesGraphique());
        chart.setTitle("Répartition des magnitudes");
        return chart;
    }

    private ObservableList<PieChart.Data> getDonneesGraphique() {
        ObservableList<PieChart.Data> donneesGraphique = FXCollections.observableArrayList();

        int countLow = 0;
        int countMedium = 0;
        int countHigh = 0;

        for (DonneesSeisme donneesSeisme : donnees) {
            double magnitude = donneesSeisme.getMagnitude();
            if (magnitude < 4.0) {
                countLow++;
            } else if (magnitude < 7.0) {
                countMedium++;
            } else {
                countHigh++;
            }
        }

        donneesGraphique.add(new PieChart.Data("Magnitude Faible", countLow));
        donneesGraphique.add(new PieChart.Data("Magnitude Moyenne", countMedium));
        donneesGraphique.add(new PieChart.Data("Magnitude Élevée", countHigh));

        return donneesGraphique;
    }

    private TableView<DonneesSeisme> creerTableau(Slider filtreMagnitudeSlider) {
        TableView<DonneesSeisme> tableau = new TableView<>();
        TableColumn<DonneesSeisme, String> colonneRegion = new TableColumn<>("Région");
        colonneRegion.setCellValueFactory(data -> data.getValue().regionProperty());
        TableColumn<DonneesSeisme, Double> colonneLatitude = new TableColumn<>("Latitude");
        colonneLatitude.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getLatitude()).asObject());
        TableColumn<DonneesSeisme, Double> colonneLongitude = new TableColumn<>("Longitude");
        colonneLongitude.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getLongitude()).asObject());
        TableColumn<DonneesSeisme, Double> colonneMagnitude = new TableColumn<>("Magnitude");
        colonneMagnitude.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getMagnitude()).asObject());

        tableau.getColumns().addAll(colonneRegion, colonneLatitude, colonneLongitude, colonneMagnitude);

        FilteredList<DonneesSeisme> donneesFiltrees = new FilteredList<>(donnees);
        tableau.setItems(donneesFiltrees);

        filtreMagnitudeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            donneesFiltrees.setPredicate(donneesSeisme -> donneesSeisme.getMagnitude() >= newValue.doubleValue());
        });

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(tableau);

        return tableau;
    }

    private BarChart<String, Number> creerGraphiqueRegions() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setData(getDonneesGraphiqueRegions());
        chart.setTitle("Proportion des régions avec des séismes");
        return chart;
    }

    private ObservableList<XYChart.Series<String, Number>> getDonneesGraphiqueRegions() {
        ObservableList<XYChart.Series<String, Number>> donneesGraphique = FXCollections.observableArrayList();

        Map<String, Integer> compteRegions = new HashMap<>();
        for (DonneesSeisme donneesSeisme : donnees) {
            String region = donneesSeisme.getRegion();
            compteRegions.put(region, compteRegions.getOrDefault(region, 0) + 1);
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Proportion des régions avec des séismes");

        int totalSeismes = donnees.size();
        for (Map.Entry<String, Integer> entry : compteRegions.entrySet()) {
            String region = entry.getKey();
            int count = entry.getValue();
            double proportion = (double) count / totalSeismes;
            serie.getData().add(new XYChart.Data<>(region, proportion));
        }

        donneesGraphique.add(serie);

        return donneesGraphique;
    }
}

class DonneesSeisme {
    private final SimpleDoubleProperty latitude;
    private final SimpleDoubleProperty longitude;
    private final SimpleDoubleProperty magnitude;
    private final SimpleStringProperty region;

    public DonneesSeisme(String region, double latitude, double longitude, double magnitude) {
        this.region = new SimpleStringProperty(region);
        this.latitude = new SimpleDoubleProperty(latitude);
        this.longitude = new SimpleDoubleProperty(longitude);
        this.magnitude = new SimpleDoubleProperty(magnitude);
    }

    public String getRegion() {
        return region.get();
    }

    public SimpleStringProperty regionProperty() {
        return region;
    }

    public double getLatitude() {
        return latitude.get();
    }

    public double getLongitude() {
        return longitude.get();
    }

    public double getMagnitude() {
        return magnitude.get();
    }
}
