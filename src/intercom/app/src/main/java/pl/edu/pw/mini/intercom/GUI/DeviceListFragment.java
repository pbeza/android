package pl.edu.pw.mini.intercom.GUI;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pl.edu.pw.mini.intercom.R;

public class DeviceListFragment extends Fragment implements PeerListListener {

    private static final String LOG_TAG = "DeviceListFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final LayoutManagerType DEFAULT_LAYOUT_MANAGER_TYPE = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

    private final List<WifiP2pDevice> peers = new ArrayList<>();
    private ProgressDialog progressDialog = null;
    private WifiP2pDevice device;
    private WiFiPeerListAdapter adapter;
    private View rootView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private LayoutManagerType currentLayoutManagerType = DEFAULT_LAYOUT_MANAGER_TYPE;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        adapter.notifyDataSetChanged();
        if (peers.size() == 0) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.no_peers_found, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * It's called eg. when orientation changes.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.device_list, container, false);
        //rootView.setTag(LOG_TAG);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        if (savedInstanceState != null) {
            // Restore saved layout manager type
            currentLayoutManagerType = (LayoutManagerType) savedInstanceState.getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(currentLayoutManagerType);
        adapter = new WiFiPeerListAdapter(peers, new PeerItemClick());
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    private void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;
        final RecyclerView.LayoutManager l = recyclerView.getLayoutManager();

        if (l != null) {
            scrollPosition = ((LinearLayoutManager) l).findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                layoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
            default:
                layoutManager = new LinearLayoutManager(getActivity());
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, currentLayoutManagerType);
    }

    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(LOG_TAG, "Peer status: " + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    private class PeerItemClick implements WiFiPeerListAdapter.ViewHolder.OnItemClickListener {

        @Override
        public void onClick(WifiP2pDevice device, View view) {
            ((DeviceActionListener) getActivity()).showDetails(device);
        }
    }

    public void updateUiForThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) rootView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = (TextView) rootView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    public void clearPeers() {
        peers.clear();
        adapter.notifyDataSetChanged();
    }

    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.press_back_to_cancel), getString(R.string.finding_peers), true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(LOG_TAG, "Finding peers cancelled");
            }
        });
    }

    public interface DeviceActionListener {
        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
    }
}
