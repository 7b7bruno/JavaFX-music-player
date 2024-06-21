package com.example.musicplayer;

import com.mpatric.mp3agic.*;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class PlayerController {
    private MediaPlayer player = null;
    private boolean songLoaded = false;
    private boolean playing = false;
    public static FileFilter filter = new FileFilter() {
        public boolean accept(File file) {
            if(file.getName().endsWith(".mp3")) return true;
            else return false;
        }
    };
    static String path = "songs";
    private File selectedSong = null;
    private Playlist currentPlaylist = null;
    private int currentSongIndex = 0;
    private ArrayList<File> songs = new ArrayList<>();
    private ArrayList<Playlist> playLists = new ArrayList<>();
    private boolean autoPlay = false;
    private boolean repeat = false;

    @FXML
    private ImageView autoplayButton;
    @FXML
    private ImageView coverImage;
    @FXML
    private ImageView nextButton;
    @FXML
    private Button playButton;
    @FXML
    private ImageView playButtonIcon;
    @FXML
    private ImageView prevButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ImageView repeatButton;
    @FXML
    private Label songArtist;
    @FXML
    private ListView<Label> songList;
    @FXML
    private Label songTitle;
    @FXML
    private Label timeLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private ComboBox<String> playlistComboBox;
    @FXML
    private ComboBox<String> addToPlaylistComboBox;
    @FXML
    private Button playPlaylistButton;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    @FXML
    void playPlaylist(ActionEvent event) {
        songSelected(songs.get(0), true);
    }

    @FXML
    void addPlaylist(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("add-playlist-view.fxml"));
        Stage stage = (Stage) addButton.getScene().getWindow();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
    }

    @FXML
    void deletePlaylist(ActionEvent event) {
        if(currentPlaylist.getName().equals("All songs")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot delete All songs");
            alert.setContentText("All songs cannot be deleted.");
            alert.showAndWait();
            return;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Playlist");
            alert.setHeaderText("Are you sure you want to delete this playlist?");
            alert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                deleteCurrentPlaylist();
            }
        }
    }

    void deleteFromJSON(String playlistName) {
        try {
            URL resourceURL = getClass().getResource("data/playlists.json");
            File file = new File(resourceURL.getFile());
            String filePath = file.getAbsolutePath();
            Object obj = new JSONParser().parse(new FileReader(filePath));
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("playlists");
            for(int i = 0; i < ja.size(); i++) {
                JSONObject JSONplaylist = (JSONObject) ja.get(i);
                String name = (String) JSONplaylist.get("name");
                if(name.equals(playlistName)) {
                    ja.remove(i);
                    break;
                }
            }
            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jo.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deleteCurrentPlaylist() {
        deleteFromJSON(currentPlaylist.getName());
        resetPlayer();
    }

    void resetPlayer() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("player-view.fxml"));
            Stage stage = (Stage) addButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            stage.setScene(scene);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void next(MouseEvent event) {
        if(currentSongIndex == songs.size() - 1) {
            currentSongIndex = 0;
            songSelected(songs.get(currentSongIndex), true);
        }
        else {
            nextSong();
        }
    }

    void nextSong() {
        if(currentSongIndex < songs.size() - 1) {
            currentSongIndex++;
            songSelected(songs.get(currentSongIndex), true);
        }
        else {
            if(repeat) {
                currentSongIndex = 0;
                songSelected(songs.get(currentSongIndex), true);
            }
            else {
                currentSongIndex = 0;
                songSelected(songs.get(currentSongIndex));
            }
        }

    }

    @FXML
    void play(ActionEvent event) {
        if(selectedSong == null) {
            songSelected(songs.get(0), true);
            return;
        }
        if(!playing && !songLoaded) {
            player = new MediaPlayer(new Media(selectedSong.toURI().toString()));
            player.setOnReady(new Runnable() {
                @Override
                public void run() {
                    Duration duration = player.getMedia().getDuration();
                    if(isValidDuration(duration)) {
                        durationLabel.setText(String.format("%02d:%02d", (int) duration.toMinutes(), (int) duration.toSeconds() % 60));
                    }
                }
            });
            bindProgress(player, progressBar);
            addSeekBehavior(player, progressBar);
            player.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    if(autoPlay) {
                        nextSong();
                    }
                    else {
                        playButtonIcon.setImage(new Image(getClass().getResource("images/play-icon.png").toExternalForm()));
                        player.seek(Duration.ZERO);
                        player.pause();
                        playing = false;
                    }
                }
            });
            player.play();
            playing = true;
            songLoaded = true;
            playButtonIcon.setImage(new Image(getClass().getResource("images/pause-icon.png").toExternalForm()));
        }
        else if(!playing && songLoaded) {
            player.play();
            playing = true;
            playButtonIcon.setImage(new Image(getClass().getResource("images/pause-icon.png").toExternalForm()));
        }
        else {
            player.pause();
            playing = false;
            playButtonIcon.setImage(new Image(getClass().getResource("images/play-icon.png").toExternalForm()));
        }
    }

    @FXML
    void previous(MouseEvent event) {
        int seconds = (int) player.getCurrentTime().toSeconds();
        if(seconds < 2) {
            previousSong();
        }
        else {
            rewind();
        }
    }
    void previousSong() {
        if(currentSongIndex > 0) {
            currentSongIndex--;
            songSelected(songs.get(currentSongIndex), true);
        }
        else {
            currentSongIndex = songs.size() - 1;
            songSelected(songs.get(currentSongIndex), true);
        }
    }
    void rewind() {
        songSelected(selectedSong, true);
    }
    @FXML
    void setAutoplay(MouseEvent event) {
        if(autoPlay) {
            autoPlay = false;
            autoplayButton.setImage(new Image(getClass().getResource("images/autoplay-icon.png").toExternalForm()));
        }
        else {
            autoPlay = true;
            autoplayButton.setImage(new Image(getClass().getResource("images/autoplay-icon-selected.png").toExternalForm()));
        }
    }
    @FXML
    void setRepeat(MouseEvent event) {
        if(repeat) {
            repeat = false;
            repeatButton.setImage(new Image(getClass().getResource("images/repeat-icon.png").toExternalForm()));
        }
        else {
            repeat = true;
            repeatButton.setImage(new Image(getClass().getResource("images/repeat-icon-selected.png").toExternalForm()));
        }
    }

    void songSelected(File song, boolean play) {
        playButtonIcon.setImage(new Image(getClass().getResource("images/play-icon.png").toExternalForm()));
        if(player != null) player.stop();
        playing = false;
        songLoaded = false;
        selectedSong = song;
        currentSongIndex = getSongIndex(song);
        loadMetadata();
        if(play) {
            play(new ActionEvent());
        }
    }
    void songSelected(File song) {
        playButtonIcon.setImage(new Image(getClass().getResource("images/play-icon.png").toExternalForm()));
        if(player != null) player.stop();
        playing = false;
        songLoaded = false;
        selectedSong = song;
        currentSongIndex = getSongIndex(song);
        loadMetadata();
    }

    private Integer getSongIndex(File song) {
        return songs.indexOf(song);
    }

    void loadMetadata() {
        File song = selectedSong;
        try {
            Mp3File mp3File = new Mp3File(song);
            ID3v1 id3v1 = mp3File.getId3v1Tag();
            ID3v2 id3v2 = mp3File.getId3v2Tag();
            String title;
            String artist;
            if(id3v1 != null) {
                title = id3v1.getTitle();
                artist = id3v1.getArtist();
            }
            else if(id3v2 != null) {
                title = id3v2.getTitle();
                artist = id3v2.getArtist();
            }
            else {
                title = "Unknown";
                artist = "Unknown";
            }
            songTitle.setText(title);
            songArtist.setText(artist);
            String coverArtPath = getCoverArt();
            if(coverArtPath == null) coverArtPath = "src/main/resources/com/example/musicplayer/images/cover-placeholder.png";
            InputStream stream = new FileInputStream(coverArtPath);
            Image coverArt = new Image(stream);
            coverImage.setImage(coverArt);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCoverArt() {
        try {
            Mp3File song = new Mp3File(selectedSong);
            ID3v2 id3v2 = song.getId3v2Tag();
            if(id3v2 != null) {
                String mimeType = id3v2.getAlbumImageMimeType();
                byte[] data = id3v2.getAlbumImage();
                String filename = createFilename(mimeType);
                RandomAccessFile file = null;
                try {
                    file = new RandomAccessFile(filename, "rw");
                    file.write(data);
                }
                finally {
                    try {
                        if(file != null) {
                            System.out.println("Extracted song cover art to: " + filename);
                            file.close();
                            return filename;
                        }
                        else return null;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String createFilename(String mimeType) throws Exception {
        String extension;
        int idx;
        if((idx = mimeType.indexOf('/')) > 0) extension = "." + mimeType.substring(idx + 1).toLowerCase();
        else mimeType = extension = "." + mimeType.toLowerCase();
        Mp3File mp3File = new Mp3File(selectedSong);
        ID3Wrapper id3Wrapper = new ID3Wrapper(mp3File.getId3v1Tag(), mp3File.getId3v2Tag());
        String path = "temp/cover-art/";
        String baseFilename = path + selectedSong.getName();
        String filename;
        if(!fileExists(filename = baseFilename + extension)) return filename;
        int i = 1;
        while(true) {
            if(!fileExists(filename = baseFilename + Integer.toString(i) + extension)) return filename;
            i++;
        }
    }
    public boolean fileExists(String str) {
        File f = new File(str);
        return f.exists();
    }

    public void initialize() {
        readPlaylists();
        initComboBox();
        currentPlaylist = new Playlist("All songs", new ArrayList<String>());
        getPlaylistSongs(currentPlaylist.getName());
    }

    private void initComboBox() {
        playlistComboBox.getItems().add("All songs");
        addToPlaylistComboBox.getItems().add("All songs");
        for(Playlist playlist: playLists) {
            playlistComboBox.getItems().add(playlist.getName());
            addToPlaylistComboBox.getItems().add(playlist.getName());
        }
        playlistComboBox.setOnAction((event) -> {
            System.out.println(playlistComboBox.getSelectionModel().getSelectedItem());
            getPlaylistSongs(playlistComboBox.getSelectionModel().getSelectedItem());
        });
        addToPlaylistComboBox.setOnAction((event) -> {
            addToPlaylist(addToPlaylistComboBox.getSelectionModel().getSelectedItem());
        });
    }

    void addToPlaylist(String playlist) {
        if(selectedSong == null) {
            System.out.println("No song slected");
            return;
        }
        try {
            URL resourceURL = getClass().getResource("data/playlists.json");
            File file = new File(resourceURL.getFile());
            String filePath = file.getAbsolutePath();
            Object obj = new JSONParser().parse(new FileReader(filePath));
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("playlists");
            JSONObject JSONplaylist = null;
            for(int i = 0; i < ja.size(); i++) {
                JSONObject jso = (JSONObject) ja.get(i);
                String name = (String) jso.get("name");
                if(name.equals(playlist)) {
                    JSONplaylist = jso;
                    break;
                }
            }
            if(JSONplaylist != null) {
                ja.remove(JSONplaylist);
                ArrayList<String> currentSongs = JSONplaylist.get("songs") == null ? new ArrayList<String>() : (ArrayList<String>) JSONplaylist.get("songs");
                currentSongs.add(selectedSong.getName());
                JSONplaylist.put("songs", currentSongs);
                ja.add(JSONplaylist);
                jo.put("playlists", ja);
                FileWriter fileWriter = new FileWriter(filePath);
                fileWriter.write(jo.toJSONString());
                fileWriter.flush();
                fileWriter.close();
                resetPlayer();
            }
            else {
                System.out.println("Playlist not found");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPlaylistSongs(String name) {
        if(name.equals("All songs")) {
            getAllSongs();
        }
        else {
            Playlist playlist = new Playlist("", new ArrayList<String>());
            for(Playlist list : playLists) {
                if(list.getName().equals(name)) {
                    playlist = list;
                    break;
                }
            }
            ArrayList<File> allSongs = getSongs(path);
            ArrayList<File> playlistSongs = new ArrayList<>();
            for(File song : allSongs) {
                if(playlist.getSongs().contains(song.getName())) {
                    playlistSongs.add(song);
                }
            }
            ObservableList<Label> songItems = FXCollections.observableArrayList();
            for(File song: playlistSongs) {
                Label songButton = createSongButton(song);
                if(songButton != null) {
                    songItems.add(songButton);
                }
            }
            songList.setItems(songItems);
            songList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null) {
                    if(autoPlay) {
                        songSelected(new File(path + "/" + newValue.getText()), true);
                    }
                    else {
                        songSelected(new File(path + "/" + newValue.getText()));
                    }
                }
            });
            songs = playlistSongs;
            System.out.println(playlistSongs);
            currentPlaylist = new Playlist(name, playlist.getSongs());
        }
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
        songList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                if(autoPlay) {
                    songSelected(new File(path + "/" + newValue.getText()), true);
                }
                else {
                    songSelected(new File(path + "/" + newValue.getText()));
                }
            }
        });
        ArrayList<String> playlistSongs = new ArrayList<>();
        for(File song: songs) {
            playlistSongs.add(song.getName());
        }
        currentPlaylist = new Playlist("All songs", playlistSongs);
    }

    public static Label createSongButton(File file) {
        if(!file.exists()) return null;
        String str = file.getName();
        Label label = new Label(str);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 20px; fx-font-family: 'Roboto';");
        return label;
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
    private void bindProgress(MediaPlayer player, ProgressBar progressBar) {
        Binding binding = Bindings.createDoubleBinding(() -> {
            Duration currentTime = player.getCurrentTime();
            Duration duration = player.getMedia().getDuration();
            if(isValidDuration(currentTime) && isValidDuration(duration)) {
                return currentTime.toMillis() / duration.toMillis();
            }
            return ProgressBar.INDETERMINATE_PROGRESS;
        }, player.currentTimeProperty(), player.getMedia().durationProperty());
        progressBar.progressProperty().bind(binding);
        bindTime(player, timeLabel);
    }
    private void bindTime(MediaPlayer player, Label label) {
        Binding binding = Bindings.createStringBinding(() -> {
            Duration currentTime = player.getCurrentTime();
            if(isValidDuration(currentTime)) {
                return String.format("%02d:%02d", (int) currentTime.toMinutes(), (int) currentTime.toSeconds() % 60);
            }
            return "00:00";
        }, player.currentTimeProperty());
        label.textProperty().bind(binding);
    }
    private void addSeekBehavior(MediaPlayer player, ProgressBar bar) {
        EventHandler<MouseEvent> onClickAndOnDragHandler =
                e -> {
                    Duration duration = player.getMedia().getDuration();
                    if(isValidDuration(duration)) {
                        Duration seekTime = duration.multiply(e.getX() / bar.getWidth());
                        player.seek(seekTime);
                        e.consume();
                    }
                };
        bar.addEventHandler(MouseEvent.MOUSE_CLICKED, onClickAndOnDragHandler);
        bar.addEventHandler(MouseEvent.MOUSE_DRAGGED, onClickAndOnDragHandler);
    }
    public boolean isValidDuration(Duration d) {
        return d != null && !d.isIndefinite() && !d.isUnknown();
    }

    private void readPlaylists() {
        try {
            URL resourceURL = getClass().getResource("data/playlists.json");
            File file = new File(resourceURL.getFile());
            String filePath = file.getAbsolutePath();
            Object obj = new JSONParser().parse(new FileReader(filePath));
            JSONObject jo = (JSONObject) obj;
            JSONArray ja = (JSONArray) jo.get("playlists");
            for(int i = 0; i < ja.size(); i++) {
                JSONObject JSONplaylist = (JSONObject) ja.get(i);
                String name = (String) JSONplaylist.get("name");
                JSONArray JSONsongs = (JSONArray) JSONplaylist.get("songs");
                ArrayList<String> songs = new ArrayList<>();
                for(int j = 0; j < JSONsongs.size(); j++) {
                    songs.add((String) JSONsongs.get(j));
                }
                playLists.add(new Playlist(name, songs));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
