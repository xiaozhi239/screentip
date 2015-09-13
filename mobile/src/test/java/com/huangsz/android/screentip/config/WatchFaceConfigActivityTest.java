package com.huangsz.android.screentip.config;

import android.content.Intent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.huangsz.android.screentip.BuildConfig;
import com.huangsz.android.screentip.R;
import com.huangsz.android.screentip.widget.ColorChooserDialog;
import com.huangsz.android.screentip.widget.TextConfigDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class WatchFaceConfigActivityTest {

    private ActivityController<WatchFaceConfigActivity> activityController;

    private WatchFaceConfigActivity configActivity;

    @Mock
    private ColorChooserDialog colorChooserDialog;

    @Mock
    private TextConfigDialog textConfigDialog;

    @Mock
    private WatchFaceConfigConnector watchFaceConfigConnector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activityController =
                Robolectric.buildActivity(WatchFaceConfigActivity.class).create().visible();
        configActivity = activityController.get();
    }

    @Test
    public void clickAndShowDialog() {
        configActivity.mTickColorDialog = colorChooserDialog;
        configActivity.mTextConfigDialog = textConfigDialog;

        configActivity.findViewById(R.id.configuration_ticks_colour_layout).performClick();
        verify(colorChooserDialog, times(1)).show(configActivity.getFragmentManager(), "");

        configActivity.findViewById(R.id.configuration_hands_colour_layout).performClick();
        verify(colorChooserDialog, times(1)).show(configActivity.getFragmentManager(), "");

        configActivity.findViewById(R.id.configuration_character_text_layout).performClick();
        verify(textConfigDialog, times(1)).show(configActivity.getFragmentManager(), "");
    }

    @Test
    public void showImageViewer() {
        configActivity.findViewById(R.id.configuration_background).performClick();
        Intent intent = Shadows.shadowOf(configActivity).getNextStartedActivity();
        assertEquals(Intent.ACTION_CHOOSER, intent.getAction());
        assertEquals("Select Pictures", intent.getStringExtra(Intent.EXTRA_TITLE));
        Intent extraIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        assertEquals(Intent.ACTION_GET_CONTENT, extraIntent.getAction());
        assertEquals("image/*", extraIntent.getType());
    }

    @Test
    public void updateConfig() {
        configActivity.mWatchFaceConfigConnector = watchFaceConfigConnector;
        configActivity.findViewById(R.id.configuration_update_button).performClick();
        verify(watchFaceConfigConnector, times(1)).sendConfigChangeToWatch();
    }

    @Test
    public void startAndStop() {
        configActivity.mWatchFaceConfigConnector = watchFaceConfigConnector;
        activityController.start();
        verify(watchFaceConfigConnector, times(1)).maybeConnect();

        activityController.stop();
        verify(watchFaceConfigConnector, times(1)).maybeDisconnect();
    }
}
