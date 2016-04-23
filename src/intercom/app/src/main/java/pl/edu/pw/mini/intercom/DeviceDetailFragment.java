package pl.edu.pw.mini.intercom;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
//import android.support.v4.app.Fragment; // TODO vs android.app.Fragment
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {

    private static final String LOG_TAG = "DeviceDetailFragment";
    private static final String IMAGE_MIME_TYPE = "image/*";
    private static final String IMAGE_FILE_EXTENSION = ".jpg";
    private static final String IMAGE_FILENAME_PREFIX = "wifip2pshared";
    private static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View contentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    private ProgressDialog progressDialog = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_device_detail, null);
        contentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final WifiP2pConfig config = new WifiP2pConfig();
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
                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);

            }
        });

        contentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                }
        );

        contentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType(IMAGE_MIME_TYPE);
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                }
        );

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) contentView.findViewById(R.id.status_text);
        statusText.setText(getResources().getString(R.string.sending_file, uri));
        Log.d(LOG_TAG, "Intent----------- " + uri);
        FileTransferService.startFileTransferService(getActivity(), uri, info);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        TextView view = (TextView) contentView.findViewById(R.id.group_owner);
        final String amIgroupOwner = info.isGroupOwner ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
        final String groupOwnerText = getResources().getString(R.string.group_owner_text) + amIgroupOwner;
        view.setText(groupOwnerText);

        view = (TextView) contentView.findViewById(R.id.device_info);
        final String groupOwnerIP = getResources().getString(R.string.group_owner_ip, info.groupOwnerAddress.getHostAddress());
        view.setText(groupOwnerIP);

        // After the group negotiation, we assign the group owner as the file server.
        // The file server is single threaded, single connection server socket.
        if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(getActivity(), contentView.findViewById(R.id.status_text)).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the get file button.
            contentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            final TextView statusTextView = (TextView) contentView.findViewById(R.id.status_text);
            statusTextView.setText(getResources().getString(R.string.client_text));
        }
        contentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) contentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) contentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    public void resetViews() {
        contentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        final int[] idsToReset = {
                R.id.device_address,
                R.id.device_info,
                R.id.group_owner,
                R.id.status_text
        };
        for (int id : idsToReset) {
            final TextView view = (TextView) contentView.findViewById(id);
            view.setText(R.string.empty);
        }
        contentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        final private Context context;
        final private TextView statusText;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final ServerSocket serverSocket = new ServerSocket(FileTransferService.PORT_NUMBER);
                Log.d(LOG_TAG, "Server: Socket opened");
                final Socket client = serverSocket.accept();
                Log.d(LOG_TAG, "Server: connection done");
                final File f = new File(generateFilename());
                final File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();
                Log.d(LOG_TAG, "Server: copying files " + f.toString());
                final InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Log.w(LOG_TAG, "onPostExecute() result is null!");
                return;
            }
            statusText.setText(context.getString(R.string.file_copied_successfully, result));
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            final Uri imgUri = Uri.parse("file://" + result);
            intent.setDataAndType(imgUri, IMAGE_MIME_TYPE);
            context.startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
            statusText.setText(R.string.opening_server_socket);
        }

        protected String generateFilename() {
            return String.format("%s/%s/%s-%d%s",
                    Environment.getExternalStorageDirectory(),
                    context.getPackageName(),
                    IMAGE_FILENAME_PREFIX,
                    System.currentTimeMillis(),
                    IMAGE_FILE_EXTENSION);
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        final int bufSize = 1024;
        final byte buf[] = new byte[bufSize];
        int len;
        try {
            final int offset = 0;
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, offset, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString(), e);
            return false;
        }
        return true;
    }

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    private OnFragmentInteractionListener mListener;
//
//    public DeviceDetailFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment DeviceDetailFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static DeviceDetailFragment newInstance(String param1, String param2) {
//        DeviceDetailFragment fragment = new DeviceDetailFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_device_detail, container, false);
//    }
//
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    @Override
//    public void onConnectionInfoAvailable(WifiP2pInfo info) {
//
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p/>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
