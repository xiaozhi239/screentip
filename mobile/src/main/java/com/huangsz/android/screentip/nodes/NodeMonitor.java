package com.huangsz.android.screentip.nodes;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * A monitor monitors connected nodes (devices), multiple nodes feature is supported after
 * google play services 7.0. But AVD doesn't support 7.0 yet, so we can't use it for emulator.
 * So currently there are at most 1 watch paired at a time.
 *
 * <p>TODO(huangsz) Change to using CapabilityApi when 7.0 is supported for emulator.
 */
public class NodeMonitor {

    private static NodeMonitor sNodeMonitor;

    private List<Node> mNodes;

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
                            setNodes(result.getNodes());
                        }
                    }
                }
        );
    }

    public boolean isEmpty() {
        return mNodes == null || mNodes.isEmpty();
    }

    public void setNodes(List<Node> nodes) {
        mNodes = nodes;
    }

    public boolean hasAvailableNode() {
        return getAvailableNode() != null;
    }

    /**
     * Get an available node. Now just return the first one, since there is at most one connected.
     */
    public @Nullable Node getAvailableNode() {
        return !isEmpty() ? mNodes.get(0) : null;
    }

    public void removeNode(Node node) {
        for (Node peer : mNodes) {
            if (peer.getId().equals(node)) {
                mNodes.remove(peer);
            }
            break;
        }
    }
}
