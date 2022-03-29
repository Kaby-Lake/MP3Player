package cn.edu.nottingham.hnyzx3.mp3player.components.musicItem;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import java.io.Serializable;

public class MusicItemViewModel extends ViewModel implements Serializable {
    public Bitmap cover;
    public String name;
    public String artist;
    public int duration;
    public String path;
    public Uri uri;
    public ObservableBoolean isSelected = new ObservableBoolean(false);

    public MusicItemViewModel() {
        Bitmap emptyBitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(emptyBitmap);
        canvas.drawColor(Color.GRAY);
        this.cover = emptyBitmap;
        this.name = "Unknown Music";
        this.artist = "Unknown Artist";
        this.duration = 0;
        this.path = "";
        this.uri = null;
    }

    // BindingAdapter for ImageView to set the image with bitmap
    @BindingAdapter("android:src")
    public static void setCover(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }
}
