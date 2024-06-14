module com.example.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.musicplayer to javafx.fxml;
    exports com.example.musicplayer;
}