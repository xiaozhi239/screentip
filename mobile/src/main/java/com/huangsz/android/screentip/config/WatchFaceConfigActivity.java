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
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.connect.model.WeatherModel;
import com.huangsz.android.screentip.data.location.LocationTracker;
import com.huangsz.android.screentip.common.feature.FLAGS;
import com.huangsz.android.screentip.data.persist.ConfigModelPersistenceManager;
import com.huangsz.android.screentip.nodes.NodeMonitor;
import com.huangsz.android.screentip.widget.ColorChooserDialog;
import com.huangsz.android.screentip.widget.ShareWatchFaceDialog;
import com.huangsz.android.screentip.widget.TextConfigDialog;
import com.huangsz.android.screentip.widget.WeatherConfigDialog;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity for configuration of the customization on the watch face.
 */
public class WatchFaceConfigActivity extends ActionBarActivity {

    private static final int CODE_SELECT_BACKGROUND_PICTURE = 0;

    private static final long DISMISS_SNAPSHOT_WAITING_IN_MILLIS = 5000;

    private static final String KEY_SAVED_CONFIG_STATE = "saved_config_state";

    private View mConfigTickColorPreview;

    private View mConfigHandColorPreview;

    private TextView mConfigTextPreview;

    private TextView mConfigWeatherTextPreview;

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

    protected WeatherConfigDialog mWeatherConfigDialog;

    private ProgressDialog mProgressDialog;

    private ConfigModelPersistenceManager mConfigPersistManager;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(KEY_SAVED_CONFIG_STATE,
                mWatchFaceConfigConnector.getSyncedConfigModel().toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        mConfigTickColorPreview = findViewById(R.id.configuration_ticks_colour_preview);
        mConfigHandColorPreview = findViewById(R.id.configuration_hands_colour_preview);
        mConfigTextPreview =
                (TextView) findViewById(R.id.configuration_character_text_preview);
        mConfigWeatherTextPreview =
                (TextView) findViewById(R.id.configuration_weather_preview);
        mUpdateConfigButton = (Button) findViewById(R.id.configuration_update_button);
        mBackgroundImageView = (ImageView) findViewById(R.id.configuration_background);
        mNodeMonitor = NodeMonitor.getInstance();
        mWatchFaceConfigConnector =
                new WatchFaceConfigConnector(this, mNodeMonitor, mConfigHandler);
        mConfigPersistManager = new ConfigModelPersistenceManager(this);

        // Recover from bundle first, if fails, recover from persistence. If nothing was saved
        // in persistence, then an empty ConfigModel will be returned.
        if (savedInstanceState != null) {
            mWatchFaceConfigConnector.setSyncedConfigModel(
                    savedInstanceState.getBundle(KEY_SAVED_CONFIG_STATE));
        } else {
            mWatchFaceConfigConnector.setSyncedConfigModel(
                    mConfigPersistManager.retrieveConfigModel());
        }

        recoverState();
        setupListeners();
    }

    private void recoverState() {
        ConfigModel updatedConfigModel = mWatchFaceConfigConnector.getSyncedConfigModel();
        if (updatedConfigModel.maybeGetTickColor() != null) {
            mConfigTickColorPreview
                    .setBackgroundColor(Color.parseColor(updatedConfigModel.maybeGetTickColor()));
        }
        if (updatedConfigModel.maybeGetHandColor() != null) {
            mConfigHandColorPreview
                    .setBackgroundColor(Color.parseColor(updatedConfigModel.maybeGetHandColor()));
        }
        if (updatedConfigModel.maybeGetTextConfigModel() != null) {
            setTextPreview(updatedConfigModel.maybeGetTextConfigModel());
        }
        if (updatedConfigModel.maybeGetWeatherModel() != null) {
            setWeatherPreview(updatedConfigModel.maybeGetWeatherModel());
        }

        // TODO(huangsz): Update the dialogs when needed.
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
                                         mConfigHandler,
                                         ConfigHandler.MESSAGE_TEXT);
                             }
                            mTextConfigDialog.show(getFragmentManager(), "");
                        }
                    }
            );
        } else {
            characterTextLayout.setVisibility(View.GONE);
        }

        // weather dialog
        // TODO: udpate weather dialog according to current weather information. Also there is a
        // bug that the weather is 0.0 each time, only get the actual data on the second time.
        RelativeLayout weatherLayout =
                (RelativeLayout) findViewById(R.id.configuration_weather_layout);
        if (FLAGS.SCREEN_WEATHER) {
            // TODO: Remove this when we enable input the city name.
            if (!LocationTracker.getInstance(this).isActive()) {
                weatherLayout.setEnabled(false);
            } else {
                weatherLayout.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mWeatherConfigDialog == null) {
                                    mWeatherConfigDialog = WeatherConfigDialog.newInstance(
                                            getString(R.string.watchface_weather),
                                            mConfigHandler,
                                            ConfigHandler.MESSAGE_WEATHER);
                                }
                                mWeatherConfigDialog.show(getFragmentManager(), "");
                            }
                        }
                );
            }
        } else {
            weatherLayout.setVisibility(View.GONE);
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
                    // Persist the configuration at the same time.
                    // TODO: The following should be run in background thread.
                    mConfigPersistManager.persistConfigModel(
                            mWatchFaceConfigConnector.getSyncedConfigModel());
                    mConfigPersistManager.persistConfigModel(
                            mWatchFaceConfigConnector.getSyncedConfigModel());
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
        if (LocationTracker.getInstance(this).isActive()) {
            LocationTracker.getInstance(this).start();
        }
    }

    @Override
    protected void onStop() {
        if (LocationTracker.getInstance(this).isActive()) {
            LocationTracker.getInstance(this).stop();
        }
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
                getResources().getString(R.string.watchface_snapshot_share),
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

    private void setTextPreview(TextConfigModel textModel) {
        mConfigTextPreview.setTextColor(Color.parseColor(textModel.maybeGetColor()));
        mConfigTextPreview.setText(textModel.maybeGetContent());
    }

    private void setWeatherPreview(WeatherModel weatherModel) {
        TextConfigModel textModel = weatherModel.getTextConfigModel();
        mConfigWeatherTextPreview.setTextColor(Color.parseColor(textModel.maybeGetColor()));
        mConfigWeatherTextPreview.setText(textModel.maybeGetContent());
    }

    public static class ConfigHandler extends Handler {

        public static final int MESSAGE_TICK_COLOR = 1;

        public static final int MESSAGE_HAND_COLOR = 2;

        public static final int MESSAGE_TEXT = 3;

        public static final int MESSAGE_SNAPSHOT_SHARE = 4;

        public static final int MESSAGE_SNAPSHOT_LOADED = 5;

        public static final int MESSAGE_SNAPSHOT_TIMEOUT = 6;

        public static final int MESSAGE_WEATHER = 7;

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
                        activity.setTextPreview(textModel);
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
                case MESSAGE_WEATHER:
                    if (FLAGS.SCREEN_WEATHER) {
                        WeatherModel weatherModel = (WeatherModel) msg.obj;
                        activity.setWeatherPreview(weatherModel);
                        activity.mWatchFaceConfigConnector.setWeatherModel(weatherModel);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
