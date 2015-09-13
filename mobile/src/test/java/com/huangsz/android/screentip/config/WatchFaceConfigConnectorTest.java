package com.huangsz.android.screentip.config;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;
import com.huangsz.android.screentip.BuildConfig;
import com.huangsz.android.screentip.connect.ConnectManager;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.widget.TextConfigDialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class WatchFaceConfigConnectorTest {

    @Mock private Context mAppContext;

    private WatchFaceConfigConnector connector;

    private ConfigModel configModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        connector = new WatchFaceConfigConnector(RuntimeEnvironment.application);
    }

    @Test
    public void setTickColor() {
        connector.setTickColor("white");
        assertEquals("white",
                connector.getConfigModel().getDataMap().getString(ConfigModel.KEY_TICK_COLOR));
    }

    @Test
    public void setHandColor() {
        connector.setHandColor("red");
        assertEquals("red",
                connector.getConfigModel().getDataMap().getString(ConfigModel.KEY_HAND_COLOR));
    }

    @Test
    public void setTextConfigModel() {
        TextConfigModel model = new TextConfigModel();
        connector.setTextConfigModel(model);
        assertEquals(model, connector.getConfigModel().maybeGetTextConfigModel());
    }

    // GoogleApiClient needs an Android wear to be connected. So we can't test the receive here.
    // And it also throws an exception complaining not able to connect.
    @Test(expected = RuntimeException.class)
    public void sendConfigChangeToWatch() {
        connector.setTickColor("white");
        connector.setHandColor("red");
        TextConfigModel textConfigModel = createTextConfigModel();
        connector.setTextConfigModel(textConfigModel);
        Bitmap background = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        connector.setBackgroundImage(background);

        assertFalse(connector.getConfigModel().isEmpty());
        assertEquals(textConfigModel, connector.getConfigModel().maybeGetTextConfigModel());
        connector.sendConfigChangeToWatch();
        assertTrue(connector.getConfigModel().isEmpty());
    }

    private TextConfigModel createTextConfigModel() {
        TextConfigModel textModel = new TextConfigModel();
        textModel.setColor("green");
        textModel.setContent("content");
        textModel.setTextSize(14);
        textModel.setCoordinateX(20);
        textModel.setCoordinateY(30);
        return textModel;
    }
}
