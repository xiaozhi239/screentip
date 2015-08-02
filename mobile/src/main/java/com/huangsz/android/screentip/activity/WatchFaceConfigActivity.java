package com.huangsz.android.screentip.activity;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.widget.CharacterTipDialog;
import com.huangsz.android.screentip.widget.ColorChooserDialog;

public class WatchFaceConfigActivity extends ActionBarActivity implements
        ColorChooserDialog.Listener, CharacterTipDialog.Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WatchFaceConfigActivity";

    private static final String TAG_CHARACTER_COLOR = "TAG_CHARACTER_COLOR";

    private static final String TAG_TICK_COLOR = "TAG_TICK_COLOR";

    private static final String TAG_CHARACTER_TEXT = "TAG_CHARACTER_TEXT";

    private static final String DATA_LAYER_WATCH_FACE_CONFIG_PATH = "/watch_face_config";

    private View mConfigCharacterColorPreview;

    private View mConfigTickColorPreview;

    private TextView mConfigCharacterTextPreview;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        mConfigCharacterColorPreview = findViewById(R.id.configuration_character_color_preview);
        mConfigTickColorPreview = findViewById(R.id.configuration_ticks_colour_preview);
        mConfigCharacterTextPreview =
                (TextView) findViewById(R.id.configuration_character_text_preview);
        findViewById(R.id.configuration_character_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorChooserDialog.newInstance(
                                getString(R.string.watchface_character_color))
                                .show(getFragmentManager(), TAG_CHARACTER_COLOR);
                    }
                });
        findViewById(R.id.configuration_ticks_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorChooserDialog.newInstance(getString(R.string.watchface_ticks_color))
                                .show(getFragmentManager(), TAG_TICK_COLOR);
                    }
                }
        );
        findViewById(R.id.configuration_character_text_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new CharacterTipDialog().show(getFragmentManager(), TAG_CHARACTER_TEXT);
                    }
                }
        );
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
    public void onColourSelected(String color, String tag) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(
                DATA_LAYER_WATCH_FACE_CONFIG_PATH);
        if (TAG_CHARACTER_COLOR.equals(tag)) {
            mConfigCharacterColorPreview.setBackgroundColor(Color.parseColor(color));
            putDataMapRequest.getDataMap().putString(TAG_CHARACTER_COLOR, color);
        } else if (TAG_TICK_COLOR.equals(tag)) {
            mConfigTickColorPreview.setBackgroundColor(Color.parseColor(color));
            putDataMapRequest.getDataMap().putString(TAG_TICK_COLOR, color);
        }
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }

    @Override
    public void onTextChanged(String text, String tag) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(
                DATA_LAYER_WATCH_FACE_CONFIG_PATH);
        if (TAG_CHARACTER_TEXT.equals(tag)) {
            mConfigCharacterTextPreview.setText(text);
            putDataMapRequest.getDataMap().putString(TAG_CHARACTER_TEXT, text);
        }
        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Applications should disable UI components that require the service,
        // and wait for a call to onConnected(Bundle) to re-enable them.
        Log.e(TAG, "Connection suspended with cause : " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
