package com.finalproject;

/**
 * Song Option class, is used to help store and parse data stored in songs.json.
 */
public class SongOption {
    protected String song;
    protected String genre;
    protected String image;
    protected String songName;

    SongOption(String songName, String genre, String image, String song) {
        this.songName = songName;
        this.genre = genre;
        this.image = image;
        this.song = song;
    }

    /**
     * Getter for song object.
     * Used in RecyclerView adapter.
     *
     * @return current Song name;
     */
    public String getSong() {
        return song;
    }
}

