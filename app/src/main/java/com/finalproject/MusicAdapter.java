package com.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private final List<SongOption> musicOptions;
    private final RecyclerViewClickListener listener;

    public MusicAdapter(List<SongOption> musicOptions, RecyclerViewClickListener listener) {
        this.musicOptions = musicOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.music_item, parent,
                false);
        return new MusicViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        holder.bind(musicOptions.get(position));
    }

    @Override
    public int getItemCount() {
        return musicOptions.size();
    }

    /*
     Overrides the onClick method, adapter position is passed to fragment;
     click listeners are set there.
     */
    public interface RecyclerViewClickListener {
        void onClick(View activeView, int pos);
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        private final ImageView albumImage;
        protected ImageButton playButton;
        private final TextView songName;
        private final TextView genre;
        private final RecyclerViewClickListener songListener;

        public MusicViewHolder(@NonNull View itemView, RecyclerViewClickListener songListener) {
            super(itemView);
            this.songListener = songListener;
            context = itemView.getContext();
            albumImage = itemView.findViewById(R.id.image_view_album_image);
            songName = itemView.findViewById(R.id.text_view_song_name);
            genre = itemView.findViewById(R.id.text_view_genre);
            playButton = itemView.findViewById(R.id.image_button_play_button);
            playButton.setOnClickListener(this);
            TooltipCompat.setTooltipText(playButton, context.getString(R.string.play_icon_tooltip));
        }

        public void bind(SongOption music) {
            int imageID = context.getResources()
                    .getIdentifier(music.image, "drawable", context.getPackageName());
            albumImage.setImageResource(imageID);
            genre.setText(music.genre);
            songName.setText(music.songName);
        }

        /**
         * Click listener is set to play button, within music item recycler view.
         *
         * @param activeView The view that was clicked.
         */
        @Override
        public void onClick(View activeView) {
            if (songListener != null) {
                songListener.onClick(activeView, getAdapterPosition());
            }
        }
    }
}
