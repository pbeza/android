package pl.edu.pw.mini.intercom.gui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.edu.pw.mini.intercom.R;
import pl.edu.pw.mini.intercom.config.EchoConfigApplication;
import pl.edu.pw.mini.intercom.connection.p2p.WifiConfig;
import pl.edu.pw.mini.intercom.connection.socket.EchoService;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final String LOG_TAG = "DeviceDetailFragment";
    private View contentView;
    private WifiP2pDevice device;
    private ProgressDialog progressDialog;
    private static boolean connectionEstablished = false; // FIXME temporary 'hotfix' (note it MUST be static)

    private class ConnectOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = ProgressDialog.show(getActivity(),
                    getString(R.string.press_back_to_cancel),
                    getString(R.string.connecting_to_device, device.deviceAddress),
                    true,
                    true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
            );
            WifiConfig wifiConfig = WifiConfig.getInstance();
            wifiConfig.connect(config);
        }
    }

    private class DisconnectOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            WifiConfig wifiConfig = WifiConfig.getInstance();
            wifiConfig.disconnect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        contentView = inflater.inflate(R.layout.fragment_device_detail, null);
        contentView.findViewById(R.id.btn_connect).setOnClickListener(new ConnectOnClickListener());
        contentView.findViewById(R.id.btn_disconnect).setOnClickListener(new DisconnectOnClickListener());
        return contentView;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        View fragmentView = getView();
        assert fragmentView != null;
        fragmentView.setVisibility(View.VISIBLE);
        setGroupOwnerText(info);
        setDeviceInfoText(info);
        if (info.groupFormed && !connectionEstablished) {
            connectionEstablished = true;
            String groupOwnerHostAddress = info.groupOwnerAddress.getHostAddress();
            EchoService.startEchoService(getActivity());
            Activity activity = getActivity();
            EchoConfigApplication echoConfigApplication = (EchoConfigApplication) activity.getApplication();
            EchoService echoService = echoConfigApplication.getEchoService();
            echoService.startSocketConnection(info.isGroupOwner, groupOwnerHostAddress);
            contentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        } else {
            Log.e(LOG_TAG, "Group was not formed");
            return;
        }
    }

    private void setDeviceInfoText(WifiP2pInfo info) {
        TextView view = (TextView) contentView.findViewById(R.id.device_info);
        String groupOwnerIP = getString(R.string.group_owner_ip, info.groupOwnerAddress.getHostAddress());
        view.setText(groupOwnerIP);
    }

    private void setGroupOwnerText(WifiP2pInfo info) {
        TextView view = (TextView) contentView.findViewById(R.id.group_owner);
        String amIGroupOwner = getString(info.isGroupOwner ? R.string.yes : R.string.no);
        String groupOwnerText = getString(R.string.group_owner_text) + amIGroupOwner;
        view.setText(groupOwnerText);
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) contentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) contentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }

    public void clearViews() {
        contentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        final int[] idsToReset = {
                R.id.device_address,
                R.id.device_info,
                R.id.group_owner,
                R.id.status_text
        };
        for (int id : idsToReset) {
            TextView view = (TextView) contentView.findViewById(id);
            view.setText("");
        }
        contentView.findViewById(R.id.btn_launch_gallery).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }
}
