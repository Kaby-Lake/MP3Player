package cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;
import cn.edu.nottingham.hnyzx3.mp3player.pages.app.App;
import cn.edu.nottingham.hnyzx3.mp3player.pages.app.AppViewModel;
import cn.edu.nottingham.hnyzx3.mp3player.utils.Notification;

public class BackgroundMusicPlayerService extends Service {

    private final String TAG = this.getClass().getSimpleName();

    public enum ConnectionState {
        Disconnected,
        Connected
    }

    /**
     * the Service's connection state
     * the service will destroy itself if the connection state is disconnected
     * else the service will continue to run (because it is connected to the activity)
     */
    private ConnectionState activityConnectionState = ConnectionState.Disconnected;

    private Notification notification = null;

    @Override
    public void onCreate() {
        super.onCreate();
        notification = new Notification(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "Service is bound");
        activityConnectionState = ConnectionState.Connected;
        return new PlayMusicBinder();
    }

    private MediaPlayer mediaPlayer;

    // the music item that is currently playing
    private MusicItemViewModel currentMusic;

    class PlayMusicBinder extends Binder implements IBackgroundMusicPlayer {

        public PlayMusicBinder() {
            mediaPlayer = new MediaPlayer();
            activityConnectionState = ConnectionState.Connected;

            // when the music has completed playing, the music player will be destroyed if the activity is not connected
            // else will remain connected
            mediaPlayer.setOnCompletionListener((mediaPlayer) -> {
                activity.whenServiceNotifyMusicStopped();
                mediaPlayer.stop();
                mediaPlayer.reset();
                if (activityConnectionState != ConnectionState.Connected) {
                    Log.e(TAG, "Service is destroyed because the music is stopped");
                    stopService();
                }
            });
            mediaPlayer.setOnPreparedListener((mp) -> {
                Log.d(TAG, String.valueOf(mp));
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                mp.reset();
                Log.e(TAG, String.valueOf(what));
                return false;
            });
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
        }

        /**
         * use the MusicPlayer to play the music file from the given MusicItemViewModel
         */
        @Override
        public void playMusic(MusicItemViewModel music) {
            if (null != mediaPlayer) {

                // if still playing, then stop the previous music
                this.stopMusicPlay();
                try {
                    // if music has a uri, then it is an external music file opened from other apps
                    // else it is a local music file (selected from the playlist)
                    if (music.uri != null) {
                        mediaPlayer.setDataSource(getApplicationContext(), music.uri);
                    } else {
                        mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(music.path));
                    }
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    notification.createNotification("Music Playing", music.path);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), "Failed to play music from path: " + music.path, Toast.LENGTH_LONG).show();
                }
                currentMusic = music;
            }
        }

        /**
         * pause the MusicPlayer if it is playing, else no effect
         */
        @Override
        public void pauseMusicPlay() {
            if (null != mediaPlayer) {
                mediaPlayer.pause();
            }
        }

        /**
         * continue to play the MusicPlayer if it is paused, else no effect
         */
        @Override
        public void continueMusicPlay() {
            if (null != mediaPlayer) {
                mediaPlayer.start();
            }
        }

        /**
         * stop and reset the MusicPlayer if it is paused or playing, else no effect
         */
        @Override
        public void stopMusicPlay() {
            if (null != mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        }

        /**
         * set the current position of the MusicPlayer
         */
        @Override
        public void seekToPosition(int position) {
            if (null != mediaPlayer) {
                mediaPlayer.seekTo(position);
            }
        }

        /**
         * get the current state of the MusicPlayer
         * used when bounded to an existing service
         */
        @Override
        public BackgroundMusicPlayerServiceState getServiceState() {
            BackgroundMusicPlayerServiceState state = new BackgroundMusicPlayerServiceState();
            try {
                state.musicStatus = mediaPlayer.isPlaying() ? AppViewModel.MusicStatus.PLAYING : AppViewModel.MusicStatus.PAUSED;
                state.currentPlayingMusic = currentMusic;
                state.currentMusicDuration = mediaPlayer.getCurrentPosition();
                state.totalMusicDuration = mediaPlayer.getDuration();
            } catch (Exception e) {
                state.musicStatus = AppViewModel.MusicStatus.STOPPED;
            }
            return state;
        }

        /**
         * expose the MediaPlayer entity used in this service
         *
         * @return the MediaPlayer entity
         */
        @Override
        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }

        /**
         * expose this BackgroundMusicPlayerService
         *
         * @return the BackgroundMusicPlayerService entity
         */
        @Override
        public BackgroundMusicPlayerService getServiceInstance() {
            return BackgroundMusicPlayerService.this;
        }

    }

    IAppCallback activity;

    /**
     * activity register to the service as callback client
     */
    public void registerClient(App activity) {
        this.activity = activity;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "Service is unbound");
        activityConnectionState = ConnectionState.Disconnected;

        // check if the music player is playing
        // if it is playing, then do not destroy the service until the current music has finished playing
        // else destroy the service immediately
        if (!mediaPlayer.isPlaying()) {
            Log.e(TAG, "Service is destroyed because mediaPlayer is not playing");
            stopService();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        Log.e(TAG, "Service is destroyed because been killed");
        stopService();
    }

    /**
     * the necessary steps to normally stop the service
     */
    private void stopService() {
        notification.clearNotification();
        mediaPlayer.release();
        stopSelf();
    }
}