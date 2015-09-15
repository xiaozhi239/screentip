package com.huangsz.android.screentip.connect.monitor;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * A monitor monitors connected nodes (devices), multiple nodes feature is supported after
 * google play services 7.0.
 */
public class NodeMonitor {

    private static NodeMonitor sNodeMonitor;

    private List<Node> mConnectedNodes;

    public static NodeMonitor getInstance() {
        if (sNodeMonitor == null) {
            sNodeMonitor = new NodeMonitor();
        }
        return sNodeMonitor;
    }

    public void updateConnectedNodes(GoogleApiClient googleApiClient) {
        Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        if (result.getStatus().isSuccess()) {
                            setConnectedNodes(result.getNodes());
                        }
                    }
                }
        );
    }

    public boolean isEmpty() {
        return mConnectedNodes == null || mConnectedNodes.isEmpty();
    }

    public void setConnectedNodes(List<Node> connectedNodes) {
        mConnectedNodes = connectedNodes;
    }

    public List<Node> getConnectedNodes() {
        return mConnectedNodes;
    }
}
