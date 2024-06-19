package com.example.musicplayer;

import com.mpatric.mp3agic.*;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PlayerController {
    private MediaPlayer player = null;
    private boolean songLoaded = false;
    private boolean playing = false;
    static FileFilter filter = new FileFilter() {
        public boolean accept(File file) {
            if(file.getName().endsWith(".mp3")) return true;
            else return false;
        }
    };

    static String path = "songs";

    private File selectedSong = null;

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
    void next(MouseEvent event) {

    }

    @FXML
    void play(ActionEvent event) {
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

    }

    void songSelected(File song) {
        if(player != null) player.stop();
        playing = false;
        songLoaded = false;
        selectedSong = song;
        loadMetadata();
        play(new ActionEvent());
    }

    void loadMetadata() {
        File song = selectedSong;
        try {
            Mp3File mp3File = new Mp3File(song);
            ID3v1 id3v1 = mp3File.getId3v1Tag();
            ID3v2 id3v2 = mp3File.getId3v2Tag();
            String title;
            String artist;
            int length = 0;
            if(id3v1 != null) {
                title = id3v1.getTitle();
                artist = id3v1.getArtist();
            }
            else if(id3v2 != null) {
                title = id3v2.getTitle();
                artist = id3v2.getArtist();
                length =id3v2.getLength();
            }
            else {
                title = "Unknown";
                artist = "Unknown";
                length = 0;
            }
            songTitle.setText(title);
            songArtist.setText(artist);
            durationLabel.setText(Integer.toString(length));
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

    private String createFilename(String mimeType) throws Exception {
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

    private boolean fileExists(String str) {
        File f = new File(str);
        return f.exists();
    }

    public void initialize() {
        songList.setStyle("-fx-background-color: #222222;");
        ArrayList<File> songs = getSongs(path);
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
                songSelected(new File(path + "/" + newValue.getText()));
            }
        });
    }

    private static Label createSongButton(File file) {
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

    private boolean isValidDuration(Duration d) {
        return d != null && !d.isIndefinite() && !d.isUnknown();
    }
}
