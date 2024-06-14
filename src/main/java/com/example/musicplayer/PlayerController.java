package com.example.musicplayer;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


public class PlayerController {
    static FileFilter filter = new FileFilter() {
        public boolean accept(File file) {
            if(file.getName().endsWith(".mp3")) return true;
            else return false;
        }
    };

    static String path = "songs";

    @FXML
    private ImageView coverImage;

    @FXML
    private ImageView nextButton;

    @FXML
    private Button playButton;

    @FXML
    private ImageView prevButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label songArtist;

    @FXML
    private VBox songList;

    @FXML
    private Label songTitle;

    @FXML
    void next(MouseEvent event) {

    }

    @FXML
    void play(ActionEvent event) {

    }

    @FXML
    void previous(MouseEvent event) {

    }

    public void initialize() {
        ArrayList<File> songs = getSongs(path);
        System.out.println(songs);
    }

    private static ArrayList<File> getSongs(String path) {
        ArrayList<File> songs = new ArrayList<>();
        File file = new File(path);
        File[] files = file.listFiles();
        if(files == null) return null;
        for(File f : files) {
            if(!f.isDirectory() && f.exists()) {
                if(filter.accept(f)) {
                    songs.add(f);
                }
            }
        }
        return songs;
    }

}
