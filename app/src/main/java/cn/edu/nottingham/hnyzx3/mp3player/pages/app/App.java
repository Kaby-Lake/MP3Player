package cn.edu.nottingham.hnyzx3.mp3player.pages.app;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import cn.edu.nottingham.hnyzx3.mp3player.R;
import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;
import cn.edu.nottingham.hnyzx3.mp3player.databinding.PageAppBinding;
import cn.edu.nottingham.hnyzx3.mp3player.pages.colorChooser.ColorChooser;
import cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer.BackgroundMusicPlayerService;
import cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer.BackgroundMusicPlayerServiceState;
import cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer.IAppCallback;
import cn.edu.nottingham.hnyzx3.mp3player.services.BackgroundMusicPlayer.IBackgroundMusicPlayer;
import cn.edu.nottingham.hnyzx3.mp3player.utils.MusicScanner;
import cn.edu.nottingham.hnyzx3.mp3player.utils.TimeIntervalExecutorService;

public class App extends AppCompatActivity implements IAppCallback {

    private final String TAG = this.getClass().getSimpleName();

    // viewModels
    AppViewModel app;
    CommonViewModel common;
    MusicListViewModel musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PageAppBinding binding = DataBindingUtil.setContentView(this, R.layout.page_app);
        // find the cached view model, if doesn't exist, create a new one
        binding.setApp(app = new AppViewModel(this));
        binding.setCommon(common = new CommonViewModel(this));
        binding.setMusicList(musicList = new MusicListViewModel(this));
        binding.activityUsersRecycler.setLayoutManager(new LinearLayoutManager(this));

        // if it is code start, bind the service
        bindAndConnectToService();

        ((SeekBar) findViewById(R.id.progressBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int tempProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                timeIntervalExecutorServiceCallback.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                app.currentMusicDuration.set(tempProgress);
                iMusicPlay.seekToPosition(tempProgress);
                timeIntervalExecutorServiceCallback.restart();
            }
        });

        if (ContextCompat.checkSelfPermission(
                this, READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "permission is granted");
            this.onScanMusicFromFileClick(null);
        } else {
            Log.e(TAG, "granting permission");
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.e(TAG, "permission granted");
                    this.onScanMusicFromFileClick(null);
                } else {
                    Log.e(TAG, "permission not granted");
                    Toast.makeText(getApplicationContext(), "MP3Player needs file read permission to access musics located in /Music", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
//        this.onScanMusicFromFileClick(null);

    }

    private void handlePlaybackIntentFromExternal(Intent possibleIntent) {
        Intent intent = possibleIntent != null ? possibleIntent : getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().toLowerCase().contains("view")) {
            Log.e(TAG, "handlePlaybackIntentFromExternal: " + intent);
            // Handle intents with specific audio/* MIME data
            Uri uri = intent.getData();
            this.onPlay(MusicScanner.getMusicFromUri(uri));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePlaybackIntentFromExternal(intent);
    }

    BackgroundMusicPlayerService musicPlayerService;

    private IBackgroundMusicPlayer iMusicPlay;

    private ServiceConnection connection;

    private void bindAndConnectToService() {
        // bind to the service if it is already running, else create a new one and bind to it
        startService(new Intent(this, BackgroundMusicPlayerService.class));
        if (null == iMusicPlay || null == iMusicPlay.getMediaPlayer()) {
            bindService(new Intent(this, BackgroundMusicPlayerService.class), connection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.e("App", "OnServiceConnected");

                    // enable the messaging from the activity to the service
                    iMusicPlay = (IBackgroundMusicPlayer) service;

                    // enable the messaging from the service to the activity
                    musicPlayerService = iMusicPlay.getServiceInstance();
                    musicPlayerService.registerClient(App.this);
                    // restore the music playing state if possible
                    BackgroundMusicPlayerServiceState previousState = iMusicPlay.getServiceState();
                    app.restoreServiceState(previousState);
                    if (previousState.musicStatus == AppViewModel.MusicStatus.PLAYING) {
                        startTimeProgressTracker();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.e("App", "onServiceDisconnected");
                }
            }, 0);
        }
    }

    /**
     * here we use the ActivityResultLauncher() to delegate StartActivityForResult()
     * this provides a callback paradigm to handle the result provided by that page
     */
    ActivityResultLauncher<Intent> colorChooserResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        int color = result.getData().getExtras().getInt(getString(R.string.color));
                        common.selectedBackgroundColor.set(color);
                    } catch (Exception ignored) {
                    }
                }
            });

    public void navigateToColorChooserPage(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.color), common.selectedBackgroundColor.get()); // key, value
        Intent intent = new Intent(this, ColorChooser.class);
        intent.putExtras(bundle);
        colorChooserResultLauncher.launch(intent);
    }

    public void onScanMusicFromFileClick(View view) {
        ArrayList<MusicItemViewModel> musics = MusicScanner.scanMusicFromExternalPath("Music");
        musicList.musicList.clear();
        musicList.musicList.addAll(musics);
        // restore the current playing music's status if possible
        musicList.setCurrentMusicStatus(app.currentPlayingMusic.get());
    }


    public void onMusicClick(View view) {
        MusicItemViewModel thisMusic = musicList.getMusicByPath((String) view.getTag());
        this.onPlay(thisMusic);
    }

    /**
     * get called when rotation happens
     * will apply the new landscape/portrait layout
     * and restore the viewModels to the new layout
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PageAppBinding binding = DataBindingUtil.setContentView(this, R.layout.page_app);
        binding.setApp(app);
        binding.setCommon(common);
        binding.setMusicList(musicList);
        binding.activityUsersRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "Activity onDestroy");
        if (null != connection) {
            stopTimeProgressTracker();
            unbindService(connection);
        }
    }

    public void onPlay(MusicItemViewModel music) {
        app.currentPlayingMusic.set(music);
        app.musicPlayingStatus.set(AppViewModel.MusicStatus.PLAYING);
        musicList.setCurrentMusicStatus(music);
        iMusicPlay.playMusic(music);
        startTimeProgressTracker();
    }

    public void onPlayPauseClick(View view) {
        if (null != iMusicPlay) {
            // if the music is playing, pause it
            if (app.musicPlayingStatus.get() == AppViewModel.MusicStatus.PLAYING) {
                iMusicPlay.pauseMusicPlay();
                app.musicPlayingStatus.set(AppViewModel.MusicStatus.PAUSED);
            } else {
                // if the music is paused, resume it
                iMusicPlay.continueMusicPlay();
                app.musicPlayingStatus.set(AppViewModel.MusicStatus.PLAYING);
            }
        }
    }

    public void onStopMusicClick(View view) {
        if (null != iMusicPlay) {
            iMusicPlay.stopMusicPlay();
            app.currentPlayingMusic.set(null);
            app.musicPlayingStatus.set(AppViewModel.MusicStatus.STOPPED);
            musicList.setCurrentMusicStatus(null);
            this.stopTimeProgressTracker();
        }
    }

    TimeIntervalExecutorService.TimeIntervalExecutorServiceCallback timeIntervalExecutorServiceCallback;

    private void startTimeProgressTracker() {
        // if the last setInterval task is not finished, cancel it
        this.stopTimeProgressTracker();
        app.totalMusicDuration.set(iMusicPlay.getMediaPlayer().getDuration());
        if (null != iMusicPlay) {
            timeIntervalExecutorServiceCallback = TimeIntervalExecutorService.scheduleSingletonAtFixedTime(0, () -> {
                Log.d("App", "getTimePlayed: " + iMusicPlay.getMediaPlayer().getCurrentPosition());
                app.currentMusicDuration.set(iMusicPlay.getMediaPlayer().getCurrentPosition());
            }, 1000);
        }
    }

    private void stopTimeProgressTracker() {
        if (null != timeIntervalExecutorServiceCallback) {
            timeIntervalExecutorServiceCallback.cancel();
        }
        app.totalMusicDuration.set(0);
        app.currentMusicDuration.set(0);
    }

    /**
     * the service notify the activity that the music has stopped
     */
    @Override
    public void whenServiceNotifyMusicStopped() {
        app.musicPlayingStatus.set(AppViewModel.MusicStatus.STOPPED);
        musicList.setCurrentMusicStatus(null);
        this.stopTimeProgressTracker();
    }
}