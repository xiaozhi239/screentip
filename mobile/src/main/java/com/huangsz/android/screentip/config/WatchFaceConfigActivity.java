package com.huangsz.android.screentip.config;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.annotations.VisibleForTesting;
import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.common.utils.ImageUtils;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.connect.tasks.LoadBitmapAsyncTask;
import com.huangsz.android.screentip.feature.FLAGS;
import com.huangsz.android.screentip.widget.ColorChooserDialog;
import com.huangsz.android.screentip.widget.ShareWatchFaceDialog;
import com.huangsz.android.screentip.widget.TextConfigDialog;

import java.lang.ref.WeakReference;

public class WatchFaceConfigActivity extends ActionBarActivity {

    private static final int CODE_SELECT_BACKGROUND_PICTURE = 0;

    private View mConfigTickColorPreview;

    private View mConfigHandColorPreview;

    private TextView mConfigTextPreview;

    private Button mUpdateConfigButton;

    private ImageView mBackgroundImageView;

    @VisibleForTesting
    protected WatchFaceConfigConnector mWatchFaceConfigConnector;

    private final Handler mConfigHandler = new ConfigHandler(this);

    @VisibleForTesting
    protected ColorChooserDialog mTickColorDialog;

    @VisibleForTesting
    protected ColorChooserDialog mHandColorDialog;

    @VisibleForTesting
    protected TextConfigDialog mTextConfigDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        mConfigTickColorPreview = findViewById(R.id.configuration_ticks_colour_preview);
        mConfigHandColorPreview = findViewById(R.id.configuration_hands_colour_preview);
        mConfigTextPreview =
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
                        if (mTickColorDialog == null) {
                            mTickColorDialog = ColorChooserDialog.newInstance(
                                    getString(R.string.watchface_ticks_color),
                                    mConfigHandler, ConfigHandler.MESSAGE_TICK_COLOR);
                        }
                        mTickColorDialog.show(getFragmentManager(), "");
                    }
                }
        );

        // hand color dialog.
        findViewById(R.id.configuration_hands_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mHandColorDialog == null) {
                            mHandColorDialog = ColorChooserDialog.newInstance(
                                    getString(R.string.watchface_hands_color),
                                    mConfigHandler, ConfigHandler.MESSAGE_HAND_COLOR);
                        }
                        mHandColorDialog.show(getFragmentManager(), "");
                    }
                });

        // text dialog.
        RelativeLayout characterTextLayout = (RelativeLayout) findViewById(
                R.id.configuration_character_text_layout);
        if (FLAGS.SCREEN_TEXT) {
            characterTextLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                             if (mTextConfigDialog == null) {
                                 mTextConfigDialog = TextConfigDialog.newInstance(
                                         getString(R.string.watchface_text),
                                         mConfigHandler, ConfigHandler.MESSAGE_TEXT);
                             }
                            mTextConfigDialog.show(getFragmentManager(), "");
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.config_action_share) {
            return requestWatchFaceSnapshot();
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

    private LoadBitmapAsyncTask.PostExecuteCallback mLoadSnapshotCallback =
            new LoadBitmapAsyncTask.PostExecuteCallback() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap) {
            // TODO(huangsz): end the sign of waiting here.
            ShareWatchFaceDialog dialog = ShareWatchFaceDialog.newInstance(
                    getResources().getString(R.string.watchface_share),
                    mConfigHandler, ConfigHandler.MESSAGE_SNAPSHOT, bitmap);
            dialog.show(getFragmentManager(), "");
        }
    };

    private boolean requestWatchFaceSnapshot() {
        mWatchFaceConfigConnector.sendSnapshotRequestToWatch(mLoadSnapshotCallback);
        // TODO(huangsz): give a sign of waiting here (with a timeout, if timeout, give a toast).
        return true;
    }

    private void shareWatchFaceSnapshot(Bitmap snapshot) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, ImageUtils.getImageUriFromBitmap(
                snapshot, getContentResolver()));
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    private static class ConfigHandler extends Handler {

        private static final int MESSAGE_TICK_COLOR = 1;

        private static final int MESSAGE_HAND_COLOR = 2;

        private static final int MESSAGE_TEXT = 3;

        private static final int MESSAGE_SNAPSHOT = 4;

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
                        activity.mConfigTextPreview.setTextColor(
                                Color.parseColor(textModel.getColor()));
                        activity.mConfigTextPreview.setText(
                                textModel.getDataMap().getString(TextConfigModel.KEY_TEXT_CONTENT));
                        activity.mWatchFaceConfigConnector.setTextConfigModel(textModel);
                    }
                    break;
                case MESSAGE_SNAPSHOT:
                    Bitmap snapshot = (Bitmap) msg.obj;
                    activity.shareWatchFaceSnapshot(snapshot);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
