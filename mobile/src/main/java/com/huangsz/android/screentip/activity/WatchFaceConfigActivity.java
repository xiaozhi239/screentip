package com.huangsz.android.screentip.activity;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.widget.ColorChooserDialog;

public class WatchFaceConfigActivity extends ActionBarActivity implements ColorChooserDialog.Listener {

    private static final String TAG_CHARACTER_COLOR = "TAG_CHARACTER_COLOR";

    private static final String TAG_TICK_COLOR = "TAG_TICK_COLOR";

    private View mConfigCharacterColorPreview;

    private View mConfigTickColorPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        mConfigCharacterColorPreview = findViewById(R.id.configuration_character_color_preview);
        mConfigTickColorPreview = findViewById(R.id.configuration_ticks_colour_preview);
        findViewById(R.id.configuration_character_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ColorChooserDialog(getString(R.string.watchface_character_color))
                                .show(getFragmentManager(), TAG_CHARACTER_COLOR);
                    }
                });
        findViewById(R.id.configuration_ticks_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ColorChooserDialog(getString(R.string.watchface_ticks_color))
                                .show(getFragmentManager(), TAG_TICK_COLOR);
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_watch_face_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColourSelected(String color, String tag) {
        if (TAG_CHARACTER_COLOR.equals(tag)) {
            mConfigCharacterColorPreview.setBackgroundColor(Color.parseColor(color));
        } else if (TAG_TICK_COLOR.equals(tag)) {
            mConfigTickColorPreview.setBackgroundColor(Color.parseColor(color));
        }
    }
}
