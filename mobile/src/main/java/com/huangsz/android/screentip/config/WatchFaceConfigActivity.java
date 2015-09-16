package com.huangsz.android.screentip.config;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.google.common.annotations.VisibleForTesting;
import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.common.utils.HintUtils;
import com.huangsz.android.screentip.common.utils.ImageUtils;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.feature.FLAGS;
import com.huangsz.android.screentip.nodes.NodeMonitor;
import com.huangsz.android.screentip.widget.ColorChooserDialog;
import com.huangsz.android.screentip.widget.ShareWatchFaceDialog;
import com.huangsz.android.screentip.widget.TextConfigDialog;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class WatchFaceConfigActivity extends ActionBarActivity {

    private static final int CODE_SELECT_BACKGROUND_PICTURE = 0;

    private static final long DISMISS_SNAPSHOT_WAITING_IN_MILLIS = 5000;

    private View mConfigTickColorPreview;

    private View mConfigHandColorPreview;

    private TextView mConfigTextPreview;

    private Button mUpdateConfigButton;

    private ImageView mBackgroundImageView;

    @VisibleForTesting
    protected WatchFaceConfigConnector mWatchFaceConfigConnector;

    private NodeMonitor mNodeMonitor;

    private final Handler mConfigHandler = new ConfigHandler(this);

    @VisibleForTesting
    protected ColorChooserDialog mTickColorDialog;

    @VisibleForTesting
    protected ColorChooserDialog mHandColorDialog;

    @VisibleForTesting
    protected TextConfigDialog mTextConfigDialog;

    private ProgressDialog mProgressDialog;

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
        mNodeMonitor = NodeMonitor.getInstance();
        mWatchFaceConfigConnector = new WatchFaceConfigConnector(this, mNodeMonitor, mConfigHandler);
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
                if (mWatchFaceConfigConnector.isConnectedToWear()) {
                    mWatchFaceConfigConnector.sendConfigChangeToWatch();
                } else {
                    HintUtils.showNoPairedWatchToast(WatchFaceConfigActivity.this);
                }
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

    private void receiveWatchFaceSnapshot(Bitmap bitmap) {
        HintUtils.dismissDialog(mProgressDialog);
        ShareWatchFaceDialog dialog = ShareWatchFaceDialog.newInstance(
                getResources().getString(R.string.watchface_share),
                mConfigHandler, ConfigHandler.MESSAGE_SNAPSHOT_SHARE, bitmap);
        dialog.show(getFragmentManager(), "");
    }

    private void failWatchFaceSnapshot() {
        Toast.makeText(this, getString(R.string.toast_fail_snapshot), Toast.LENGTH_LONG).show();
    }

    private boolean requestWatchFaceSnapshot() {
        if (mWatchFaceConfigConnector.isConnectedToWear()) {
            showSnapshotProgressDialog();
            mWatchFaceConfigConnector.sendSnapshotRequestToWatch();
        } else {
            HintUtils.showNoPairedWatchToast(this);
        }
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

    private void showSnapshotProgressDialog() {
        mProgressDialog = HintUtils.showProgressDialog(this,
                getString(R.string.watchface_snapshot_progress),
                getString(R.string.hint_loading),
                true);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    Message message = new Message();
                    message.what = ConfigHandler.MESSAGE_SNAPSHOT_TIMEOUT;
                    mConfigHandler.sendMessage(message);
                }
            }
        }, DISMISS_SNAPSHOT_WAITING_IN_MILLIS);
    }

    public static class ConfigHandler extends Handler {

        public static final int MESSAGE_TICK_COLOR = 1;

        public static final int MESSAGE_HAND_COLOR = 2;

        public static final int MESSAGE_TEXT = 3;

        public static final int MESSAGE_SNAPSHOT_SHARE = 4;

        public static final int MESSAGE_SNAPSHOT_LOADED = 5;

        public static final int MESSAGE_SNAPSHOT_TIMEOUT = 6;

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
                case MESSAGE_SNAPSHOT_SHARE:
                    Bitmap shareSnapshot = (Bitmap) msg.obj;
                    activity.shareWatchFaceSnapshot(shareSnapshot);
                    break;
                case MESSAGE_SNAPSHOT_LOADED:
                    Bitmap snapshot = (Bitmap) msg.obj;
                    activity.receiveWatchFaceSnapshot(snapshot);
                    break;
                case MESSAGE_SNAPSHOT_TIMEOUT:
                    activity.failWatchFaceSnapshot();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
