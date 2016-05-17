package pl.edu.pw.mini.intercom.GUI;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.edu.pw.mini.intercom.R;
import pl.edu.pw.mini.intercom.connection.p2p.DeviceActionListener;
import pl.edu.pw.mini.intercom.connection.socket.EchoService;

public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final String LOG_TAG = "DeviceDetailFragment";
    private View contentView = null;
    private WifiP2pDevice device;
    private ProgressDialog progressDialog = null;
    private EchoService echoService;
    private final ServiceConnection serviceConnection = new EchoServiceConnection();
    private final Handler msgQueueHandler = new EchoHandler();
    private boolean isServiceStarted = false;

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
                    getResources().getString(R.string.press_back_to_cancel),
                    getResources().getString(R.string.connecting_to_device, device.deviceAddress),
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
            ((DeviceActionListener) getActivity()).connect(config);
        }
    }

    private class DisconnectOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ((DeviceActionListener) getActivity()).disconnect();
        }
    }

    private class EchoServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            echoService = ((EchoService.EchoServiceBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            echoService = null;
        }
    }

    private static class EchoHandler extends Handler {
        public void handleMessage(Message message) {
            Bundle data = message.getData();
        }
    }

    @Override
    public void onResume() {
        if (echoService == null) {
            doBindService();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // FIXME put back (?)
        if (echoService != null) {
            getActivity().getApplicationContext().unbindService(serviceConnection);
            echoService = null;
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Activity activity = getActivity();
        activity.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        contentView = inflater.inflate(R.layout.fragment_device_detail, null);
        contentView.findViewById(R.id.btn_connect).setOnClickListener(new ConnectOnClickListener());
        contentView.findViewById(R.id.btn_disconnect).setOnClickListener(new DisconnectOnClickListener());
//        contentView.findViewById(R.id.btn_launch_gallery).setOnClickListener(new LaunchGalleryOnClickListener());
        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User has picked an image. Transfer it to group owner i.e peer using FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) contentView.findViewById(R.id.status_text);
        statusText.setText(getResources().getString(R.string.sending_file, uri));
        Log.d(LOG_TAG, "Intent sending URI: " + uri);
        //FileTransferService.startFileTransferService(getActivity(), uri, info);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        View fragmentView = getView();
        assert fragmentView != null;
        fragmentView.setVisibility(View.VISIBLE);

        TextView view = (TextView) contentView.findViewById(R.id.group_owner);
        String amIGroupOwner = info.isGroupOwner ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        String groupOwnerText = getResources().getString(R.string.group_owner_text) + amIGroupOwner;
        view.setText(groupOwnerText);

        view = (TextView) contentView.findViewById(R.id.device_info);
        String groupOwnerIP = getResources().getString(R.string.group_owner_ip, info.groupOwnerAddress.getHostAddress());
        view.setText(groupOwnerIP);

        // After the group negotiation, we assign the group owner as the file server.
        // The file server is single threaded, single connection server socket.

        if (!info.groupFormed) {
            Log.e(LOG_TAG, "Group was not formed");
            return;
        }

        if (!isServiceStarted) {
            Activity activity = getActivity();
            activity.setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
            EchoService.startEchoService(activity, info.isGroupOwner, info.groupOwnerAddress.getHostAddress());
            isServiceStarted = true;
        }

        /*
        if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(getActivity(), contentView.findViewById(R.id.status_text)).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the get file button.
            contentView.findViewById(R.id.btn_launch_gallery).setVisibility(View.VISIBLE);
            TextView statusTextView = (TextView) contentView.findViewById(R.id.status_text);
            statusTextView.setText(getResources().getString(R.string.client_text));
        }
        */
        contentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    private void doBindService() {
        Intent intent = new Intent(this.getActivity(), EchoService.class);
        // Create a new Messenger for the communication back from the Service to the Activity
        Messenger messenger = new Messenger(msgQueueHandler);
        intent.putExtra(EchoService.EXTRAS_MESSENGER_PARAM, messenger);
//        intent.setAction(EchoService.ACTION_START_ECHO_PARAM);
        getActivity().getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
            view.setText(R.string.empty);
        }
        contentView.findViewById(R.id.btn_launch_gallery).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }
}
