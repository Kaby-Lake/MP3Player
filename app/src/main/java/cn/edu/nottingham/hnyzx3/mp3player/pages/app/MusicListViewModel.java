package cn.edu.nottingham.hnyzx3.mp3player.pages.app;

import android.app.Activity;

import androidx.databinding.ObservableArrayList;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

import cn.edu.nottingham.hnyzx3.mp3player.BR;
import cn.edu.nottingham.hnyzx3.mp3player.R;
import cn.edu.nottingham.hnyzx3.mp3player.bindings.recyclerview.adapter.binder.ItemBinder;
import cn.edu.nottingham.hnyzx3.mp3player.bindings.recyclerview.adapter.binder.ItemBinderBase;
import cn.edu.nottingham.hnyzx3.mp3player.components.musicItem.MusicItemViewModel;

public class MusicListViewModel extends ViewModel {

    public MusicListViewModel(Activity activity) {

    }

    /**
     * custom binder to the component inside the recycler view
     */
    public ItemBinder<MusicItemViewModel> itemViewBinder() {
        return new ItemBinderBase<>(BR.music, R.layout.components_music_item);
    }

    public ObservableArrayList<MusicItemViewModel> musicList = new ObservableArrayList<>();

    /**
     * get the MusicItemViewModel by the specified path from the music list
     * @param path
     * @return
     */
    public MusicItemViewModel getMusicByPath(String path) {
        MusicItemViewModel ret = null;
        for (MusicItemViewModel musicItemViewModel : this.musicList) {
            if (Objects.equals(musicItemViewModel.path, path)) {
                ret = musicItemViewModel;
            }
        }
        return ret;
    }

    /**
     * set the specified music to play, others set to stop
     * @param currentMusic can be null, then all music set to stop
     */
    public void setCurrentMusicStatus(MusicItemViewModel currentMusic) {
        String path = null;
        if (currentMusic != null) {
            path = currentMusic.path;
        }
        for (MusicItemViewModel musicItemViewModel : this.musicList) {
            musicItemViewModel.isSelected.set(false);
            if (path != null && Objects.equals(musicItemViewModel.path, path)) {
                musicItemViewModel.isSelected.set(true);
            }
        }
    }
}
