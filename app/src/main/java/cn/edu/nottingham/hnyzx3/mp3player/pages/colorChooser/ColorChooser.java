package cn.edu.nottingham.hnyzx3.mp3player.pages.colorChooser;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import cn.edu.nottingham.hnyzx3.mp3player.R;
import cn.edu.nottingham.hnyzx3.mp3player.databinding.PageColorChooserBinding;
import cn.edu.nottingham.hnyzx3.mp3player.pages.app.CommonViewModel;

public class ColorChooser extends AppCompatActivity {

    CommonViewModel common;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PageColorChooserBinding binding = DataBindingUtil.setContentView(this, R.layout.page_color_chooser);
        binding.setCommon(common = new CommonViewModel(this));

        // if the activity is created from the main activity, get the color from the intent
        if (savedInstanceState == null) {
            int color = getIntent().getExtras().getInt(getString(R.string.color));
            this.common.selectedBackgroundColor.set(color);
        }
    }

    /**
     * When the user clicks the color, set the color and return to the main activity
     */
    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.color), common.selectedBackgroundColor.get());
        Intent replyIntent = new Intent();
        replyIntent.putExtras(bundle);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    public void onBackClick(View view) {
        onBackPressed();
    }

    /**
     * called when rotation happens, set whether the activity is in landscape or portrait mode
     * and the UI will reacts to the changes
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        common.isLandscapeMode.set(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }
}