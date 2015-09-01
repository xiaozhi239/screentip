package com.huangsz.android.screentip.config;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.huangsz.com.screentip.common.utils.ImageUtils;
import android.huangsz.com.screentip.connect.model.TextConfigModel;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.feature.FLAGS;
import com.huangsz.android.screentip.widget.ColorChooserDialog;
import com.huangsz.android.screentip.widget.TextConfigDialog;

import java.lang.ref.WeakReference;

public class WatchFaceConfigActivity extends ActionBarActivity {

    private static final String TAG = "WatchFaceConfigActivity";

    private static final int CODE_SELECT_BACKGROUND_PICTURE = 0;

    private View mConfigTickColorPreview;

    private View mConfigHandColorPreview;

    private TextView mConfigCharacterTextPreview;

    private Button mUpdateConfigButton;

    private ImageView mBackgroundImageView;

    private WatchFaceConfigConnector mWatchFaceConfigConnector;

    private final Handler mConfigHandler = new ConfigHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        mConfigTickColorPreview = findViewById(R.id.configuration_ticks_colour_preview);
        mConfigHandColorPreview = findViewById(R.id.configuration_hands_colour_preview);
        mConfigCharacterTextPreview =
                (TextView) findViewById(R.id.configuration_character_text_preview);
        mUpdateConfigButton = (Button) findViewById(R.id.configuration_update_button);
        mBackgroundImageView = (ImageView) findViewById(R.id.configuration_background);
        mWatchFaceConfigConnector = new WatchFaceConfigConnector(this);
        setupListeners();
    }

    private void setupListeners() {

        // tick color dialog.
        findViewById(R.id.configuration_ticks_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorChooserDialog.newInstance(getString(R.string.watchface_ticks_color),
                                mConfigHandler, ConfigHandler.MESSAGE_TICK_COLOR)
                                .show(getFragmentManager(), "");
                    }
                }
        );

        // hand color dialog.
        findViewById(R.id.configuration_hands_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorChooserDialog.newInstance(getString(R.string.watchface_hands_color),
                                mConfigHandler, ConfigHandler.MESSAGE_HAND_COLOR)
                                .show(getFragmentManager(), "");
                    }
                });

        // text dialog.
        RelativeLayout characterTextLayout = (RelativeLayout) findViewById(
                R.id.configuration_character_text_layout);
        if (FLAGS.SCREEN_TEXT) {
            characterTextLayout.setOnClickListener(
                    new View.OnClickListener() {
                        private TextConfigDialog dialog;

                        @Override
                        public void onClick(View v) {
                             if (dialog == null) {
                                 dialog = TextConfigDialog.newInstance(
                                         getString(R.string.watchface_text),
                                         mConfigHandler, ConfigHandler.MESSAGE_TEXT);
                             }
                            dialog.show(getFragmentManager(), "");
                        }
                    }
            );
        } else {
            characterTextLayout.setVisibility(View.GONE);
        }

        mBackgroundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Select a picture.
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Pictures"),
                        CODE_SELECT_BACKGROUND_PICTURE);
            }
        });

        mUpdateConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWatchFaceConfigConnector.sendConfigChangeToWatch();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWatchFaceConfigConnector.maybeConnect();
    }

    @Override
    protected void onStop() {
        mWatchFaceConfigConnector.maybeDisconnect();
        super.onStop();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_SELECT_BACKGROUND_PICTURE) {
            Uri selectedImageUri = data.getData();
            Bitmap bitmap = ImageUtils.createScaledBitmap(
                    selectedImageUri, mBackgroundImageView.getWidth(),
                    mBackgroundImageView.getHeight(), this);
            mWatchFaceConfigConnector.setBackgroundImage(bitmap);
            mBackgroundImageView.setImageBitmap(bitmap);
        }
    }

    private static class ConfigHandler extends Handler {

        private static final int MESSAGE_TICK_COLOR = 1;

        private static final int MESSAGE_HAND_COLOR = 2;

        private static final int MESSAGE_TEXT = 3;

        private final WeakReference<WatchFaceConfigActivity> activityReference;

        private ConfigHandler(WatchFaceConfigActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            WatchFaceConfigActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case MESSAGE_TICK_COLOR:
                    String color = (String) msg.obj;
                    activity.mConfigTickColorPreview.setBackgroundColor(Color.parseColor(color));
                    activity.mWatchFaceConfigConnector.setTickColor(color);
                    break;
                case MESSAGE_HAND_COLOR:
                    color = (String) msg.obj;
                    activity.mConfigHandColorPreview.setBackgroundColor(Color.parseColor(color));
                    activity.mWatchFaceConfigConnector.setHandColor(color);
                    break;
                case MESSAGE_TEXT:
                    if (FLAGS.SCREEN_TEXT) {
                        TextConfigModel textModel = (TextConfigModel) msg.obj;
                        activity.mConfigCharacterTextPreview.setText(
                                textModel.getDataMap().getString(TextConfigModel.KEY_TEXT_CONTENT));
                        activity.mWatchFaceConfigConnector.setTextConfigModel(textModel);
                    }
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
