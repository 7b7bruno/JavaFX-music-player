package com.example.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                String path = "temp/cover-art";
                File[] files = new File(path).listFiles();
                for (File file : files) {
                    file.delete();
                }
                System.out.println("Deleted temp cover art files");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }));
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("player-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setTitle("Music player");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}