module fr.julien.quakescope {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens fr.julien.quakescope to javafx.fxml;
    exports fr.julien.quakescope;
}