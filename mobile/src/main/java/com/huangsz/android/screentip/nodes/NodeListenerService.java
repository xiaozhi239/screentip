package com.huangsz.android.screentip.nodes;

import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Arrays;

public class NodeListenerService extends WearableListenerService {

    private static final String TAG = "NodeListenerService";

    private NodeMonitor mNodeMonitor;

    @Override
    public void onCreate() {
        super.onCreate();
        mNodeMonitor = NodeMonitor.getInstance();
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.i(TAG, String.format("Peer[name:%s, id:%s] connected: ",
                peer.getDisplayName(), peer.getId()));
        super.onPeerConnected(peer);
        // There are only one connected watch now.
        mNodeMonitor.setNodes(new ArrayList<>(Arrays.asList(peer)));
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.i(TAG, String.format("Peer[name:%s, id:%s] disconnected: ",
                peer.getDisplayName(), peer.getId()));
        super.onPeerDisconnected(peer);
        mNodeMonitor.removeNode(peer);
    }


}
