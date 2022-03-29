package cn.edu.nottingham.hnyzx3.mp3player.pages.app;

import android.app.Activity;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.TimeUnit;

import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;
import cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer.BackgroundMusicPlayerServiceState;

public class AppViewModel extends ViewModel {

    public AppViewModel(Activity activity) {
    }

    /**
     * restore the music playing state of the app, from the connected service
     * this is useful when the app re-connects to a service that is already running
     * @param state
     */
    public void restoreServiceState(BackgroundMusicPlayerServiceState state) {
        if (state != null && state.currentPlayingMusic != null) {
            this.currentPlayingMusic.set(state.currentPlayingMusic);
            this.musicPlayingStatus.set(state.musicStatus);
            this.currentMusicDuration.set(state.currentMusicDuration);
            this.totalMusicDuration.set(state.totalMusicDuration);
        }
    }

    public enum MusicStatus {
        Error, PLAYING, PAUSED, STOPPED;
    }

    public ObservableField<MusicStatus> musicPlayingStatus = new ObservableField<>(MusicStatus.STOPPED);

    /**
     * the current playing music, if not playing, set to null;
     */
    public ObservableField<MusicItemViewModel> currentPlayingMusic = new ObservableField<>(null);

    public ObservableInt totalMusicDuration = new ObservableInt(0);

    @BindingAdapter("android:text")
    public static void setDuration(TextView view, int duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        view.setText(String.format("%02d:%02d", minutes, seconds));
    }


    public ObservableInt currentMusicDuration = new ObservableInt(0);

}