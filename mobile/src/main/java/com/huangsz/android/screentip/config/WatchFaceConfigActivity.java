package com.huangsz.android.screentip.config;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.feature.FLAGS;
import com.huangsz.android.screentip.utils.ImageUtils;
import com.huangsz.android.screentip.widget.CharacterTipDialog;
import com.huangsz.android.screentip.widget.ColorChooserDialog;

public class WatchFaceConfigActivity extends ActionBarActivity {

    private static final String TAG = "WatchFaceConfigActivity";

    private static final int CODE_SELECT_BACKGROUND_PICTURE = 0;

    private static final int MESSAGE_TICK_COLOR = 1;

    private static final int MESSAGE_HAND_COLOR = 2;

    private static final int MESSAGE_TEXT_COLOR = 3;

    private static final int MESSAGE_TEXT = 4;

    private View mConfigCharacterColorPreview;

    private View mConfigTickColorPreview;

    private View mConfigHandColorPreview;

    private TextView mConfigCharacterTextPreview;

    private Button mUpdateConfigButton;

    private ImageView mBackgroundImageView;

    private WatchFaceConfigConnector mWatchFaceConfigConnector;

    private final Handler mConfigHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TICK_COLOR:
                    String color = (String) msg.obj;
                    mConfigTickColorPreview.setBackgroundColor(Color.parseColor(color));
                    mWatchFaceConfigConnector.setTickColor(color);
                    break;
                case MESSAGE_HAND_COLOR:
                    color = (String) msg.obj;
                    mConfigHandColorPreview.setBackgroundColor(Color.parseColor(color));
                    mWatchFaceConfigConnector.setHandColor(color);
                    break;
                case MESSAGE_TEXT_COLOR:
                    if (FLAGS.SCREEN_CHARACTER) {
                        color = (String) msg.obj;
                        mConfigCharacterColorPreview.setBackgroundColor(Color.parseColor(color));
                        mWatchFaceConfigConnector.setCharacterColor(color);
                    }
                    break;
                case MESSAGE_TEXT:
                    if (FLAGS.SCREEN_CHARACTER) {
                        String text = (String) msg.obj;
                        mConfigCharacterTextPreview.setText(text);
                        mWatchFaceConfigConnector.setCharacterText(text);
                    }
                default:
                    super.handleMessage(msg);
            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_face_config);
        mConfigCharacterColorPreview = findViewById(R.id.configuration_character_color_preview);
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
        // text color dialog.
        RelativeLayout characterColorLayout = (RelativeLayout) findViewById(
                R.id.configuration_character_colour_layout);
        if (FLAGS.SCREEN_CHARACTER) {
            characterColorLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ColorChooserDialog.newInstance(
                                    getString(R.string.watchface_character_color),
                                    mConfigHandler, MESSAGE_TEXT_COLOR)
                                    .show(getFragmentManager(), "");
                        }
                    });
        } else {
            characterColorLayout.setVisibility(View.GONE);
        }

        // tick color dialog.
        findViewById(R.id.configuration_ticks_colour_layout).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorChooserDialog.newInstance(getString(R.string.watchface_ticks_color),
                                mConfigHandler, MESSAGE_TICK_COLOR)
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
                                mConfigHandler, MESSAGE_HAND_COLOR)
                                .show(getFragmentManager(), "");
                    }
                });

        // text dialog.
        RelativeLayout characterTextLayout = (RelativeLayout) findViewById(
                R.id.configuration_character_text_layout);
        if (FLAGS.SCREEN_CHARACTER) {
            characterTextLayout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new CharacterTipDialog().show(getFragmentManager(), "");
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
//
//    @Override
//    public void onTextChanged(String text, String tag) {
//        if (FLAGS.SCREEN_CHARACTER && TAG_CHARACTER_TEXT.equals(tag)) {
//            mConfigCharacterTextPreview.setText(text);
//            mWatchFaceConfigConnector.setCharacterText(text);
//        }
//    }

}
