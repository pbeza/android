package pl.edu.pw.mini.intercom.gui;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
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
import pl.edu.pw.mini.intercom.audio.AudioConfig;
import pl.edu.pw.mini.intercom.config.EchoConfigApplication;
import pl.edu.pw.mini.intercom.connection.p2p.WifiConfig;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = "MainActivity";
    private final IntentFilter intentFilter = new IntentFilter();
    private EchoConfigApplication config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        config = (EchoConfigApplication) getApplication();
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
        if (!config.isEchoServiceBinded()) {
            config.bindEchoService();
        }
        WifiConfig wifiConfig = WifiConfig.getInstance(this);
        registerReceiver(wifiConfig, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (config.isEchoServiceBinded()) {
            config.unbindEchoService();
        }
        WifiConfig wifiConfig = WifiConfig.getInstance(this);
        unregisterReceiver(wifiConfig); // TODO possibly better use static receiver within AndroidManifest.xml
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
//    }

    public void clearViews() {
        clearPeers();
        clearDetails();
    }

    private void clearPeers() {
        FragmentManager fragmentManager = getFragmentManager();
        DeviceListFragment fragmentList = (DeviceListFragment) fragmentManager.findFragmentById(R.id.frag_list);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
    }

    private void clearDetails() {
        FragmentManager fragmentManager = getFragmentManager();
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) fragmentManager.findFragmentById(R.id.frag_detail);
        if (fragmentDetails != null) {
            fragmentDetails.clearViews();
        }
    }

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
        return item.getItemId() == R.id.action_settings ? true : super.onOptionsItemSelected(item);
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

    public void discoverPeers(View v) {
        WifiConfig wifiConfig = WifiConfig.getInstance(this);
        boolean isWifiEnabled = wifiConfig.isWifiP2pEnabled();
        if (!isWifiEnabled) {
            Toast.makeText(this, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
//            Snackbar.make(view, R.string.floating_button_snackbar_text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        DeviceListFragment fragment = (DeviceListFragment) fragmentManager.findFragmentById(R.id.frag_list);
        fragment.onInitiateDiscovery();
        wifiConfig.discoverPeers(this);
    }

    public void toggleMuteVolume(View view) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AudioConfig audioConfig = AudioConfig.getInstance(audioManager);
        boolean isPlaying = audioConfig.togglePlaying();
        FloatingActionButton b = (FloatingActionButton) findViewById(R.id.muteFloatingActionButton);
        b.setImageResource(isPlaying ? R.drawable.ic_volume_up_black_24dp : R.drawable.ic_volume_off_black_24dp);
    }

    public void showWirelessSettings(MenuItem menuItem) {
//        WifiConfig wifiConfig = WifiConfig.getInstance();
//        if (wifiConfig.manager != null && wifiConfig.channel != null) {
        // Since this is the system wireless settings activity, it's not going to send us a
        // result. We will be notified by WiFiDeviceBroadcastReceiver instead.
        Intent wirelessSettings = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivity(wirelessSettings);
//        } else {
//            Log.e(LOG_TAG, "Channel or manager is null");
//        }
    }
}
