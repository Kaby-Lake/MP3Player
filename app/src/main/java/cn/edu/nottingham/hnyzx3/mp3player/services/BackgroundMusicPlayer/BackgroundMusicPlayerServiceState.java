package cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer;

import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;
import cn.edu.nottingham.hnyzx3.mp3player.pages.app.AppViewModel;

/**
 * the state class that is used to represent the state of the background music player service
 * will be used when activity asks for the state of the service
 */
public class BackgroundMusicPlayerServiceState {
    public AppViewModel.MusicStatus musicStatus = null;
    public MusicItemViewModel currentPlayingMusic = null;
    public int totalMusicDuration = 0;
    public int currentMusicDuration = 0;
}
