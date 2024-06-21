package com.example.musicplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class AddPlaylistController {
    @FXML
    private Button addButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField nameField;
    @FXML
    private ListView<Label> songList;

    static String path = "songs";
    static FileFilter filter = new FileFilter() {
        public boolean accept(File file) {
            if(file.getName().endsWith(".mp3")) return true;
            else return false;
        }
    };
    private ArrayList<File> songs = new ArrayList<>();
    private ArrayList<String> selectedSongs = new ArrayList<>();

    @FXML
    void add(ActionEvent event) throws Exception {
        if(validate()) {
            writeJsonData();
            Parent root = FXMLLoader.load(getClass().getResource("player-view.fxml"));
            Stage stage = (Stage) addButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            stage.setScene(scene);
        }
    }

    boolean validate() {
        if(nameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Name cannot be empty");
            alert.setContentText("Please enter a name.");
            alert.showAndWait();
            return false;
        }
        else {
            String pattern = "^[a-zA-Z0-9\s]+$";
            if(!nameField.getText().matches(pattern)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Name must only contain letters and numbers");
                alert.setContentText("Please enter a valid name.");
                alert.showAndWait();
                return false;
            }
            else {
                if(selectedSongs.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No songs selected");
                    alert.setContentText("Please select at least one song.");
                    alert.showAndWait();
                    return false;
                }
                else {
                    return true;
                }
            }
        }

    }

    private void writeJsonData() {
        try {
            URL resourceURL = getClass().getResource("data/playlists.json");
            File file = new File(resourceURL.getFile());
            String filePath = file.getAbsolutePath();
            Object obj = new JSONParser().parse(new FileReader(filePath));
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("playlists");
            JSONObject newPlaylist = new JSONObject();
            newPlaylist.put("name", nameField.getText());
            newPlaylist.put("songs", selectedSongs);
            ja.add(newPlaylist);
            jo.put("playlists", ja);
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jo.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void cancel(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("player-view.fxml"));
        Stage stage = (Stage) addButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
    }

    public void initialize() {
        getAllSongs();
    }

    public void getAllSongs() {
        songList.setStyle("-fx-background-color: #222222;");
        songs = getSongs(path);
        ObservableList<Label> songItems = FXCollections.observableArrayList();
        for(File song: songs) {
            Label songButton = createSongButton(song);
            if(songButton != null) {
                songItems.add(songButton);
            }
        }
        songList.setItems(songItems);

    }
    private void selectSong(Label label) {
        if(selectedSongs.contains(label.getText())) {
            selectedSongs.remove(label.getText());
            label.setStyle("-fx-background-color: #222222; -fx-text-fill: white; -fx-font-size: 20px; fx-font-family: 'Roboto';");
            return;
        }
        label.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 20px; fx-font-family: 'Roboto';");
        selectedSongs.add(label.getText());
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
    private Label createSongButton(File file) {
        if(!file.exists()) return null;
        String str = file.getName();
        Label label = new Label(str);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 20px; fx-font-family: 'Roboto';");
        label.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            selectSong(label);
        });
        return label;
    }

}
