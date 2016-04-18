package pl.edu.pw.mini.intercom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "WiFiBroadcastReceiver:";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;
    //private WifiP2pManager.PeerListListener peerListListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            wifiPeer2PeerStateChangedAction(context, intent);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            wifiPeer2PeerPeersChangedAction(context, intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            wifiPeer2PeerConnectionChangedAction(context, intent);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            wifiPeer2PeerThisDeviceChangedAction(context, intent);
        }
    }

    private void wifiPeer2PeerStateChangedAction(Context context, Intent intent) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        Log.d(LOG_TAG, "WIFI_P2P_STATE_CHANGED_ACTION (state = " + state + ")");

        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            Log.d(LOG_TAG, "Wifi P2P is enabled");
            activity.setIsWifiP2pEnabled(true);
        } else {
            Log.d(LOG_TAG, "WiFi P2P is not enabled");
            activity.setIsWifiP2pEnabled(false);
            activity.resetData();
        }
    }

    private void wifiPeer2PeerPeersChangedAction(Context context, Intent intent) {
        Log.d(LOG_TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");

        // Request available peers from the wifi p2p manager. This is an
        // asynchronous call and the calling activity is notified with a
        // callback on PeerListListener.onPeersAvailable()

        if (manager != null) {
            manager.requestPeers(channel, (WifiP2pManager.PeerListListener) activity.getFragmentManager().findFragmentById(R.id.frag_list));
        } else {
            Log.d(LOG_TAG, "WifiP2pManager is null");
        }

        Log.d(LOG_TAG, "P2P peers changed");
    }

    private void wifiPeer2PeerConnectionChangedAction(Context context, Intent intent) {
        Log.d(LOG_TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");

        if (manager == null) {
            Log.d(LOG_TAG, "WifiP2pManager is null");
            return;
        }

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if (networkInfo.isConnected()) {
            // We are connected with the other device, request connection info to find group owner IP
            DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
            manager.requestConnectionInfo(channel, fragment);
        } else {
            activity.resetData();
        }
    }

    private void wifiPeer2PeerThisDeviceChangedAction(Context context, Intent intent) {
        Log.d(LOG_TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");

        DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
        fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
    }
}