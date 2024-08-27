package com.finalproject.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finalproject.AudioService;
import com.finalproject.MusicAdapter;
import com.finalproject.SongOption;
import com.finalproject.databinding.FragmentMusicBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Starts Music Service when play-button of chosen song is clicked.
 */
public class MusicFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // view binding
        FragmentMusicBinding binding =
                FragmentMusicBinding.inflate(getLayoutInflater(), container, false);
        View fragmentMusicView = binding.getRoot();
        RecyclerView recyclerView = binding.recyclerMusic;

        // loads and parses songs from assets folder
        String json = loadJSONFromAsset();
        List<SongOption> songList = parseSongs(json);

        // music adapter's interface allows click listener to be attached here
        MusicAdapter.RecyclerViewClickListener songListener = (view, position) -> {
            // obtain song details
            String song = songList.get(position).getSong();
            int songID = requireContext().getResources().getIdentifier(song,
                    "raw", requireContext().getPackageName());

            // intent is prepared for audio service
            Intent intent = new Intent(getContext(), AudioService.class);
            intent.putExtra("song_id", songID);
            intent.putExtra("song_name", song);

            // the appropriate service is started based on API version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent);
            } else {
                requireContext().startService(intent);
            }
        };

        MusicAdapter musicAdapter = new MusicAdapter(songList, songListener);
        recyclerView.setAdapter(musicAdapter);
        return fragmentMusicView;
    }

    /**
     * Opens and parses songs.json.
     *
     * @return a string representation of the information contained in songs.json
     */
    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = requireContext().getApplicationContext().getAssets()
                    .open("songs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * Turns provided json String into a List, then returns the list.
     *
     * @param json json String formed using loadJSONFromAsset
     * @return list with SongOption parameters.
     */
    private List<SongOption> parseSongs(String json) {
        Gson gson = new Gson();
        Type dataType = new TypeToken<List<SongOption>>() {
        }.getType();
        return gson.fromJson(json, dataType);
    }
}