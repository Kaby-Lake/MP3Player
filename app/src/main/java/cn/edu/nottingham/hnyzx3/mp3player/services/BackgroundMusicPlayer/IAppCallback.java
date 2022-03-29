package cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer;

public interface IAppCallback {

    /**
     * the callback used when service notifies the activity that the music has stopped
     */
    void whenServiceNotifyMusicStopped();

}
