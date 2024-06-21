package com.example.musicplayer;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

public class Playlist {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private ArrayList<String> songs;

    public Playlist(String name, ArrayList<String> songs) {
        setName(name);
        setSongs(songs);
    }

    @Override
    public String toString() {
        return name + "[" + songs + "]";
    }
}
