module com.example.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires mp3agic;
    requires javafx.media;
    requires json.simple;
    requires junit;
    requires static lombok;


    opens com.example.musicplayer to javafx.fxml;
    exports com.example.musicplayer;
    exports com.example.musicplayer.tests;
}