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
import cn.edu.nottingham.hnyzx3.mp3player.utils.ServiceManager;
import cn.edu.nottingham.hnyzx3.mp3player.utils.TimeIntervalExecutorService;

public class App extends AppCompatActivity implements IAppCallback {

    private final String TAG = this.getClass().getSimpleName();

    // viewModels

    AppViewModel app;
    CommonViewModel common;
    MusicListViewModel musicList;


    // services and bindings

    BackgroundMusicPlayerService musicPlayerService = null;

    private IBackgroundMusicPlayer iMusicPlay = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("App", "OnServiceConnected");

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
            Log.i("App", "onServiceDisconnected");
        }
    };

    /**
     * the service notify the activity that the music has stopped
     */
    @Override
    public void whenServiceNotifyMusicStopped() {
        app.musicPlayingStatus.set(AppViewModel.MusicStatus.STOPPED);
        app.currentPlayingMusic.set(null);
        musicList.setCurrentMusicStatus(null);
        this.stopTimeProgressTracker();
    }


    // life cycle

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.i(TAG, "permission granted");
                    this.onScanMusicFromFileClick(null);
                } else {
                    Log.i(TAG, "permission not granted");
                    Toast.makeText(getApplicationContext(), "MP3Player needs file read permission to access musics located in /Music", Toast.LENGTH_LONG).show();
                }
            });

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
                if (app.musicPlayingStatus.get() == AppViewModel.MusicStatus.PLAYING) {
                    tempProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (app.musicPlayingStatus.get() == AppViewModel.MusicStatus.PLAYING) {
                    timeIntervalExecutorServiceCallback.cancel();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (app.musicPlayingStatus.get() == AppViewModel.MusicStatus.PLAYING) {
                    app.currentMusicDuration.set(tempProgress);
                    iMusicPlay.seekToPosition(tempProgress);
                    timeIntervalExecutorServiceCallback.restart();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(
                this, READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "permission is granted");
            this.onScanMusicFromFileClick(null);
        } else {
            Log.i(TAG, "granting permission");
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handlePlaybackIntentFromExternal(intent);
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
        Log.i(TAG, "Activity onDestroy");
        stopTimeProgressTracker();
        unbindService(connection);
    }


    // click listeners

    public void onPlayPauseClick(View view) {
        if (ServiceManager.isServiceRunning(this, BackgroundMusicPlayerService.class)) {
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
        if (ServiceManager.isServiceRunning(this, BackgroundMusicPlayerService.class)) {
            iMusicPlay.stopMusicPlay();
            app.currentPlayingMusic.set(null);
            app.musicPlayingStatus.set(AppViewModel.MusicStatus.STOPPED);
            musicList.setCurrentMusicStatus(null);
            this.stopTimeProgressTracker();
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
        this.playFromMusic(thisMusic);
    }


    // helper functions

    private void handlePlaybackIntentFromExternal(Intent possibleIntent) {
        Intent intent = possibleIntent != null ? possibleIntent : getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().toLowerCase().contains("view")) {
            Log.i(TAG, "handlePlaybackIntentFromExternal: " + intent);
            // Handle intents with specific audio/* MIME data
            Uri uri = intent.getData();
            this.playFromMusic(MusicScanner.getMusicFromUri(uri));
        }
    }

    private void bindAndConnectToService() {
        if (!ServiceManager.isServiceRunning(this, BackgroundMusicPlayerService.class)) {
            Log.i("App", "Service not running, start a new one");
            startService(new Intent(this, BackgroundMusicPlayerService.class));
        } else {
            Log.i("App", "Service already running");
        }
        // provide 0 to the flag, method will not start service until a call like startService(Intent) is made to start the service
        bindService(new Intent(this, BackgroundMusicPlayerService.class), connection, 0);
    }


    public void playFromMusic(MusicItemViewModel music) {
        app.currentPlayingMusic.set(music);
        app.musicPlayingStatus.set(AppViewModel.MusicStatus.PLAYING);
        musicList.setCurrentMusicStatus(music);
        iMusicPlay.playMusic(music);
        startTimeProgressTracker();
    }


    // progress tracker

    TimeIntervalExecutorService.TimeIntervalExecutorServiceCallback timeIntervalExecutorServiceCallback;

    private void startTimeProgressTracker() {
        // if the last setInterval task is not finished, cancel it
        this.stopTimeProgressTracker();
        app.totalMusicDuration.set(iMusicPlay.getMediaPlayer().getDuration());
        if (ServiceManager.isServiceRunning(this, BackgroundMusicPlayerService.class)) {
            timeIntervalExecutorServiceCallback = TimeIntervalExecutorService.scheduleSingletonAtFixedTime(0, () -> {
                int currentPosition = iMusicPlay.getMediaPlayer().getCurrentPosition();
                Log.d("App", "getTimePlayed: " + currentPosition);
                app.currentMusicDuration.set(currentPosition);
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
}