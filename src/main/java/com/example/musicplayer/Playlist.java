package com.example.musicplayer;

import java.util.ArrayList;

public class Playlist {
    private String name;
    private ArrayList<String> songs;

    public Playlist(String name, ArrayList<String> songs) {
        setName(name);
        setSongs(songs);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSongs(ArrayList<String> songs) {
        this.songs = songs;
    }

    @Override
    public String toString() {
        return name + "[" + songs + "]";
    }
}
