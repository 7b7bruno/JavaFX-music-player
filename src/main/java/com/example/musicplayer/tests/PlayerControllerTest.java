package com.example.musicplayer.tests;

import com.example.musicplayer.PlayerController;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;


public class PlayerControllerTest {
    @Test
    public void testIsValidDuration() {
        PlayerController pc = new PlayerController();
        assertTrue(pc.isValidDuration(Duration.ZERO));
        assertFalse(pc.isValidDuration(Duration.INDEFINITE));
        assertTrue(pc.isValidDuration(Duration.ONE));
        assertFalse(pc.isValidDuration(Duration.UNKNOWN));
    }

    @Test
    public void testFileExists() {
        PlayerController pc = new PlayerController();
        assertTrue(pc.fileExists("D:\\CodingProjects\\Java\\MusicPlayer\\src\\main\\resources\\com\\example\\musicplayer\\add-playlist-view.fxml"));
        assertFalse(pc.fileExists("test.txt"));
    }

    @Test
    public void filterTest() {
        PlayerController pc = new PlayerController();
        File file = new File("D:\\CodingProjects\\Java\\MusicPlayer\\src\\main\\resources\\com\\example\\musicplayer\\add-playlist-view.fxml");
        assertFalse(pc.filter.accept(file));
    }
}
