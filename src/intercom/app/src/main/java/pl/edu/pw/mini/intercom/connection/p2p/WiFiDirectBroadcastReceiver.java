package pl.edu.pw.mini.intercom.connection.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import pl.edu.pw.mini.intercom.R;
import pl.edu.pw.mini.intercom.gui.DeviceDetailFragment;
import pl.edu.pw.mini.intercom.gui.DeviceListFragment;
import pl.edu.pw.mini.intercom.gui.MainActivity;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "WiFiBroadcastReceiver";
    private final WifiP2pManager manager;
    private final WifiP2pManager.Channel channel;
    private final MainActivity activity;
    //private WifiP2pManager.PeerListListener peerListListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                stateChangedAction(intent);
                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                peersChangedAction(intent);
                break;
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                connectionChangedAction(intent);
                break;
            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                thisDeviceChangedAction(intent);
                break;
            default:
                Log.w(LOG_TAG, "Unexpected action: " + action);
        }
    }

    /*
     * WIFI_P2P_STATE_CHANGED_ACTION
     *
     * Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled.
     * An extra EXTRA_WIFI_STATE provides the state information as int.
     */
    private void stateChangedAction(Intent intent) {
        final int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        Log.d(LOG_TAG, "WIFI_P2P_STATE_CHANGED_ACTION, state = " + state);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            Log.d(LOG_TAG, "Wifi P2P is enabled");
            activity.setIsWifiP2pEnabled(true);
        } else {
            Log.d(LOG_TAG, "WiFi P2P is not enabled");
            activity.setIsWifiP2pEnabled(false);
            activity.clearViews();
        }
    }

    /*
     * WIFI_P2P_PEERS_CHANGED_ACTION
     *
     * Broadcast intent action indicating that the available peer list has changed.
     * This can be sent as a result of peers being found, lost or updated.
     *
     * An extra EXTRA_P2P_DEVICE_LIST provides the full list of current peers.
     * The full list of peers can also be obtained any time with:
     * requestPeers(WifiP2pManager.Channel, WifiP2pManager.PeerListListener).
     */
    private void peersChangedAction(Intent intent) {
        Log.d(LOG_TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
        final WifiP2pDeviceList wifiP2pDeviceList = (WifiP2pDeviceList) intent.getExtras().get(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
        Log.d(LOG_TAG, "Currently " + wifiP2pDeviceList.getDeviceList().size() + " peer(s) available");
        // Request available peers from the wifi p2p manager. This is an asynchronous call and the
        // calling activity is notified with a callback on PeerListListener.onPeersAvailable()
        if (manager != null) {
            manager.requestPeers(channel, (WifiP2pManager.PeerListListener) activity.getFragmentManager().findFragmentById(R.id.frag_list));
        } else {
            Log.w(LOG_TAG, "WifiP2pManager is null");
        }
    }

    /*
     * WIFI_P2P_CONNECTION_CHANGED_ACTION
     *
     * Broadcast intent action indicating that the state of Wi-Fi p2p connectivity has changed.
     * One extra EXTRA_WIFI_P2P_INFO provides the p2p connection info in the form of a WifiP2pInfo object.
     * Another extra EXTRA_NETWORK_INFO provides the network info in the form of a NetworkInfo.
     * A third extra (EXTRA_WIFI_P2P_GROUP) provides the details of the group.
     */
    private void connectionChangedAction(Intent intent) {
        Log.d(LOG_TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
        if (manager == null) {
            Log.w(LOG_TAG, "WifiP2pManager is null");
            return;
        }
        WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        Log.v(LOG_TAG, "WiFi P2P info: " + p2pInfo.toString());
        WifiP2pGroup p2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        Log.v(LOG_TAG, "WiFi P2P group" + p2pGroup.toString());
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if (networkInfo.isConnected()) {
            final DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
            Log.d(LOG_TAG, "We are connected - requesting connection info");
            manager.requestConnectionInfo(channel, fragment);
        } else {
            Log.d(LOG_TAG, "We are not connected - clearing data");
            activity.clearViews();
        }
    }

    /*
     * WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
     *
     * Broadcast intent action indicating that this device details have changed.
     */
    private void thisDeviceChangedAction(Intent intent) {
        Log.d(LOG_TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        final DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
        fragment.updateUiForThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
    }
}