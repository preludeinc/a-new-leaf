package com.finalproject;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


/**
 * This Audio Service class helps to facilitate music playback.
 */
public class AudioService extends Service {
    private static final int MUSIC_NOTIFICATION_ID = 1;
    private String songName;
    private MediaPlayer musicPlayer;
    private String channelID;

    public AudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    /**
     * Creates a new media player instance, and sets its volume.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        float volume_left = 0.8f;
        float volume_right = 0.8f;
        if (musicPlayer == null) {
            musicPlayer = new MediaPlayer();
            musicPlayer.setVolume(volume_left, volume_right);
        }
    }

    /**
     * This runs when the service is started.
     * Starts the notification channel and media player.
     *
     * @param intent  The intent supplied to startService(). May be null, so is important
     *                to check for this.
     * @param flags   Data about this start request.
     * @param startId An integer which represents the request to
     *                start.
     * @return an integer which indicates the service was started, but isn't restarted if killed.
     * This is useful as music should stop when the app is exited.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // song ID and name are taken from bundle,
            int songID = intent.getIntExtra("song_id", 0);
            // song name is shown in notification
            songName = intent.getStringExtra("song_name");
            notificationChannel();
            startMusic(songID);
        }
        return START_NOT_STICKY;
    }

    /**
     * Creates a notification channel for foreground service notifications (for APIs of 26 and higher)
     * Importance is set to min, as sound for these notifications is not necessary.
     */
    private void notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = getString(R.string.music_channel_name);
            channelID = getString(R.string.music_channel_id);
            int importance = NotificationManager.IMPORTANCE_MIN;
            NotificationChannel musicChannel = new NotificationChannel(channelID, channelName,
                    importance);
            NotificationManager musicNotificationManager =
                    getSystemService(NotificationManager.class);
            musicNotificationManager.createNotificationChannel(musicChannel);
        }
        startNotifications();
    }

    /**
     * Formats song name by removing underscores, and capitalizing first letter.
     * Creates a new notification and starts the foreground service.
     */
    private void startNotifications() {
        if (songName != null) {
            // removes underscores
            songName = songName.replaceAll("_", " ");
            // capitalizes first character
            songName = songName.substring(0, 1).toUpperCase() + songName.substring(1);
        }
        Notification songNotification =
                new NotificationCompat.Builder(this, channelID)
                        .setSmallIcon(R.drawable.music_icon)
                        .setContentTitle(ContextCompat.getString(this, R.string.song_playing))
                        .setContentText(songName)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(songName))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setColor(ContextCompat.getColor(this, R.color.light_green))
                        .setColorized(true)
                        .setAutoCancel(true)
                        .build();
        startForeground(MUSIC_NOTIFICATION_ID, songNotification);
    }

    /**
     * Starts music playback, if media player is playing another song it is released.
     * Looping is set to true, so the song will continue unless the user clicks mute.
     *
     * @param songID song to be played based on which carousel item is clicked.
     */
    public void startMusic(int songID) {
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        musicPlayer = MediaPlayer.create(this, songID);
        musicPlayer.setLooping(true);
        musicPlayer.start();
    }

    /**
     * Stops music playback, and execution of foreground service (and therefore notifications too).
     */
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
            stopForeground(true);
        }
    }

    /**
     * Media player instance is destroyed, and foreground service is stopped.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMusic();
        stopSelf();
    }
}