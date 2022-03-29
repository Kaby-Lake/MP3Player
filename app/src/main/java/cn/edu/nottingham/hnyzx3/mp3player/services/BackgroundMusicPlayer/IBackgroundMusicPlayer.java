package cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer;

import android.media.MediaPlayer;

import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;

public interface IBackgroundMusicPlayer {

    /**
     * use the MusicPlayer to play the music file from the given MusicItemViewModel
     */
    void playMusic(MusicItemViewModel music);

    /**
     * pause the MusicPlayer if it is playing, else no effect
     */
    void pauseMusicPlay();

    /**
     * continue to play the MusicPlayer if it is paused, else no effect
     */
    void continueMusicPlay();

    /**
     * stop and reset the MusicPlayer if it is paused or playing, else no effect
     */
    void stopMusicPlay();

    /**
     * set the current position of the MusicPlayer
     */
    void seekToPosition(int position);

    /**
     * get the current state of the MusicPlayer
     * used when bounded to an existing service
     */
    BackgroundMusicPlayerServiceState getServiceState();

    /**
     * expose the MediaPlayer entity used in this service
     * @return the MediaPlayer entity
     */
    MediaPlayer getMediaPlayer();


    /**
     * expose this BackgroundMusicPlayerService
     * @return the BackgroundMusicPlayerService entity
     */
    BackgroundMusicPlayerService getServiceInstance();
}
