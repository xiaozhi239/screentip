package com.huangsz.android.screentip.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;

import com.huangsz.android.screentip.BuildConfig;
import com.huangsz.android.screentip.connect.model.ConfigModel;
import com.huangsz.android.screentip.connect.model.TextConfigModel;
import com.huangsz.android.screentip.nodes.NodeMonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
@Config(constants = BuildConfig.class, sdk = 21)
public class WatchFaceConfigConnectorTest {

    @Mock private Context mAppContext;

    @Mock private Handler mUiHandler;

    @Mock private NodeMonitor mNodeMonitor;

    private WatchFaceConfigConnector connector;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        connector = new WatchFaceConfigConnector(RuntimeEnvironment.application,
                mNodeMonitor, mUiHandler);
    }

    @Test
    public void setTickColor() {
        connector.setTickColor("white");
        assertEquals("white", connector.getNewChangeConfigModel().maybeGetTickColor());
    }

    @Test
    public void setHandColor() {
        connector.setHandColor("red");
        assertEquals("red", connector.getNewChangeConfigModel().maybeGetHandColor());
    }

    @Test
    public void setTextConfigModel() {
        TextConfigModel model = new TextConfigModel();
        connector.setTextConfigModel(model);
        assertEquals(model, connector.getNewChangeConfigModel().maybeGetTextConfigModel());
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

        assertFalse(connector.getNewChangeConfigModel().isEmpty());
        assertEquals(textConfigModel, connector.getNewChangeConfigModel().maybeGetTextConfigModel());
        connector.sendConfigChangeToWatch();
        assertTrue(connector.getNewChangeConfigModel().isEmpty());
    }

    @Test
    public void saveAndRestoreSyncedConfigModel() {
        connector.setHandColor("red");
        connector.setTickColor("blue");
        connector.updateSyncedConfifModel();

        Bundle bundle = connector.getSyncedConfigModel().toBundle();
        connector.setSyncedConfigModel(bundle);
        ConfigModel restoredModel = connector.getSyncedConfigModel();
        assertEquals("red", restoredModel.maybeGetHandColor());
        assertEquals("blue", restoredModel.maybeGetTickColor());
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
