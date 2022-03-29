package cn.edu.nottingham.hnyzx3.mp3player.pages.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import cn.edu.nottingham.hnyzx3.mp3player.utils.ColorFactory;

public class CommonViewModel extends ViewModel {

    public CommonViewModel(Activity activity) {
        this.isLandscapeMode.set(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * the current color of the background
     */
    public ObservableInt selectedBackgroundColor = new ObservableInt(ColorFactory.getRandomColor());

    /**
     * whether it is in landscape mode
     */
    public ObservableBoolean isLandscapeMode = new ObservableBoolean(false);

    /**
     * BindingAdapter for ConstraintLayout's backgroundColor
     */
    @BindingAdapter("android:background")
    public static void _setSelectedBackgroundColor(ConstraintLayout layout, int color) {
        layout.setBackground(new ColorDrawable(color));
    }

    public void setSelectedBackgroundColor(int color) {
        this.selectedBackgroundColor.set(color);
    }
}