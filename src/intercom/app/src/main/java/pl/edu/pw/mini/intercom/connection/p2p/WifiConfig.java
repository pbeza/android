package pl.edu.pw.mini.intercom.connection.p2p;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pl.edu.pw.mini.intercom.R;
import pl.edu.pw.mini.intercom.gui.DeviceDetailFragment;
import pl.edu.pw.mini.intercom.gui.DeviceListFragment;
import pl.edu.pw.mini.intercom.gui.MainActivity;

public class WifiConfig implements WifiP2pManager.ChannelListener, DeviceActionListener {

    private static WifiConfig instance = new WifiConfig();
    private static final String LOG_TAG = "WifiConfig";
    private boolean isWifiP2pEnabled = false, retryChannel = false;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;
    private FragmentManager fragmentManager;
    private BroadcastReceiver receiver;

    private WifiConfig() {
    }

    public static WifiConfig getInstance() {
        return instance;
    }

    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }


    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    // TODO remove MainActivity reference and other GUI-related code from this class
    public void updateContextReferences(MainActivity mainActivity) {
        this.activity = mainActivity;
        this.fragmentManager = mainActivity.getFragmentManager();
        manager = (WifiP2pManager) mainActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(mainActivity, mainActivity.getMainLooper(), this);
        receiver = new WifiDirectBroadcastReceiver(manager, channel, this, activity);
    }

    public void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WifiDirectBroadcastReceiver (WIFI_P2P_PEERS_CHANGED_ACTION) will notify us
                Toast.makeText(activity, R.string.on_discovery_init_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(activity, activity.getString(R.string.on_discovery_init_failure, reasonCode), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // Try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(activity, R.string.reconnection, Toast.LENGTH_LONG).show();
            activity.clearViews();
            retryChannel = true;
            channel = manager.initialize(activity, activity.getMainLooper(), this);
        } else {
            Toast.makeText(activity, R.string.connection_lost_permanently, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) fragmentManager.findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }

    @Override
    public void cancelDisconnect() {
        // A cancel abort request by user. Disconnect i.e. removeGroup if already connected.
        // Else, request WifiP2pManager to abort the ongoing request.
        if (manager == null) {
            return;
        }
        DeviceListFragment fragment = (DeviceListFragment) fragmentManager.findFragmentById(R.id.frag_list);
        WifiP2pDevice device = fragment.getDevice();
        if (device == null || device.status == WifiP2pDevice.CONNECTED) {
            disconnect();
        } else if (device.status == WifiP2pDevice.AVAILABLE || device.status == WifiP2pDevice.INVITED) {
            manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Toast.makeText(activity, R.string.aborting_connection, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(activity, activity.getString(R.string.aborting_connection_fail, reasonCode), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WifiDirectBroadcastReceiver (WIFI_P2P_CONNECTION_CHANGED_ACTION) will notify us
                Log.d(LOG_TAG, "Connection successful");
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, R.string.connection_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) fragmentManager.findFragmentById(R.id.frag_detail);
        fragment.clearViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e(LOG_TAG, "Disconnect failed. Reason: " + reasonCode);
            }
        });
    }
}
