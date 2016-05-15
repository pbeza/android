package pl.edu.pw.mini.intercom.GUI;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pl.edu.pw.mini.intercom.R;
import pl.edu.pw.mini.intercom.connection.p2p.DeviceActionListener;
import pl.edu.pw.mini.intercom.connection.p2p.WiFiDirectBroadcastReceiver;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WifiP2pManager.ChannelListener, DeviceActionListener {

    private static final String LOG_TAG = "MainActivity";
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private boolean isWifiP2pEnabled = false, retryChannel = false;
    private FragmentManager fragmentManager;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // GUI related initialization

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Not GUI related initialization

        fragmentManager = getFragmentManager();
        addActionsToIntentFilter();
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), this);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        discoverPeers();
    }

    private void addActionsToIntentFilter() {
        String[] actions = {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION,
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION,
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION,
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
        };
        for (String action : actions) {
            intentFilter.addAction(action);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver); // TODO possibly better use static receiver within AndroidManifest.xml
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // See configChanges tag in AndroidManifest.xml to learn more
        String orientation;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                orientation = "landscape";
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                orientation = "portrait";
                break;
            default:
                orientation = "unknown";
        }
        String msg = String.format("Probably screen orientation has changed to %1$s mode", orientation);
        Log.d(LOG_TAG, msg);
    }

    public void clearViews() {
        clearPeers();
        clearDetails();
    }

    private void clearPeers() {
        DeviceListFragment fragmentList = (DeviceListFragment) fragmentManager.findFragmentById(R.id.frag_list);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
    }

    private void clearDetails() {
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) fragmentManager.findFragmentById(R.id.frag_detail);
        if (fragmentDetails != null) {
            fragmentDetails.clearViews();
        }
    }

    public boolean showWirelessSettings(MenuItem item) {
        if (manager != null && channel != null) {
            // Since this is the system wireless settings activity, it's not going to send us a
            // result. We will be notified by WiFiDeviceBroadcastReceiver instead.
            Intent wirelessSettings = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(wirelessSettings);
        } else {
            Log.e(LOG_TAG, "Channel or manager is null");
        }
        return true;
    }

    public void discoverPeers(View v) {
        if (!isWifiP2pEnabled) {
            Toast.makeText(MainActivity.this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
//            Snackbar.make(view, R.string.floating_button_snackbar_text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }
        DeviceListFragment fragment = (DeviceListFragment) fragmentManager.findFragmentById(R.id.frag_list);
        fragment.onInitiateDiscovery();
        discoverPeers();
    }

    private void discoverPeers() {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver (WIFI_P2P_PEERS_CHANGED_ACTION) will notify us
                Toast.makeText(MainActivity.this, R.string.on_discovery_init_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, getString(R.string.on_discovery_init_failure, reasonCode), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) fragmentManager.findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver (WIFI_P2P_CONNECTION_CHANGED_ACTION) will notify us
                Log.d(LOG_TAG, "Connection successful");
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, R.string.connection_failure, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onChannelDisconnected() {
        // Try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, R.string.reconnection, Toast.LENGTH_LONG).show();
            clearViews();
            retryChannel = true;
            channel = manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this, R.string.connection_lost_permanently, Toast.LENGTH_LONG).show();
        }
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
                    Toast.makeText(MainActivity.this, R.string.aborting_connection, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(MainActivity.this, getString(R.string.aborting_connection_fail, reasonCode), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /***
     * GUI below. TODO: separate GUI from WiFi logic
     ***/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on
        // the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_camera:
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_slideshow:
                break;
            case R.id.nav_manage:
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
            default:
                Log.e(LOG_TAG, "Unrecognized itemId in onNavigationItemSelected");
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}