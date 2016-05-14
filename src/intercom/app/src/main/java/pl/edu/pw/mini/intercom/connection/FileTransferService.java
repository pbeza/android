//package pl.edu.pw.mini.intercom.connection;
//
//import android.app.IntentService;
//import android.content.ContentResolver;
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.net.wifi.p2p.WifiP2pInfo;
//import android.os.Bundle;
//import android.util.Log;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.net.Socket;
//
//import pl.edu.pw.mini.intercom.GUI.DeviceDetailFragment;
//
//public class FileTransferService extends IntentService {
//
//    private static final String LOG_TAG = "FileTransferService";
//    private static final String WORKER_THREAD_NAME = LOG_TAG;
//    private static final int SOCKET_MILISEC_TIMEOUT = 5000;
//    public static final int PORT_NUMBER = 8988;
//    private static final String PACKAGE_PREFIX = "pl.edu.pw.mini.intercom.";
//    private static final String ACTION_SEND_FILE = PACKAGE_PREFIX + "SEND_FILE";
//    private static final String EXTRAS_FILE_PATH = PACKAGE_PREFIX + "file_url";
//    private static final String EXTRAS_GROUP_OWNER_ADDRESS = PACKAGE_PREFIX + "go_host";
//    private static final String EXTRAS_GROUP_OWNER_PORT = PACKAGE_PREFIX + "go_port";
//
//    public FileTransferService(String name) {
//        super(name);
//    }
//
//    public FileTransferService() {
//        super(WORKER_THREAD_NAME);
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//        if (intent == null) {
//            Log.w(LOG_TAG, "Given intent is null");
//            return;
//        }
//
//        final String action = intent.getAction();
//
//        if (action.equals(ACTION_SEND_FILE)) {
//            final Bundle extras = intent.getExtras();
//            final String fileUri = extras.getString(EXTRAS_FILE_PATH);
//            final String host = extras.getString(EXTRAS_GROUP_OWNER_ADDRESS);
//            final int port = extras.getInt(EXTRAS_GROUP_OWNER_PORT);
//            sendFileToPeer(fileUri, host, port);
//        }
//    }
//
//    private void sendFileToPeer(String fileUri, String host, int port) {
//        final Socket socket = new Socket();
//
//        try {
//            Log.d(LOG_TAG, "Opening client socket (serverHost = " + host + ", port = " + port + ")");
//            socket.bind(null);
//            socket.connect(new InetSocketAddress(host, port), SOCKET_MILISEC_TIMEOUT);
//            Log.d(LOG_TAG, "Client socket - isConnected: " + socket.isConnected());
//            final OutputStream stream = socket.getOutputStream();
//            final Context context = getApplicationContext();
//            final ContentResolver cr = context.getContentResolver();
//            InputStream is = null;
//            try {
//                is = cr.openInputStream(Uri.parse(fileUri));
//            } catch (FileNotFoundException e) {
//                Log.e(LOG_TAG, e.toString(), e);
//            }
//            DeviceDetailFragment.copyFile(is, stream);
//            Log.d(LOG_TAG, "Data written successfully");
//        } catch (IOException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//        } finally {
//            if (socket != null && socket.isConnected()) {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    Log.e(LOG_TAG, "Cannot close socket", e);
//                }
//            }
//        }
//    }
//
//    public static void startFileTransferService(Context context, Uri uri, WifiP2pInfo info) {
//        final Intent s = new Intent(context, FileTransferService.class);
//        s.setAction(ACTION_SEND_FILE);
//        s.putExtra(EXTRAS_FILE_PATH, uri.toString());
//        s.putExtra(EXTRAS_GROUP_OWNER_ADDRESS, info.groupOwnerAddress.getHostAddress());
//        s.putExtra(EXTRAS_GROUP_OWNER_PORT, PORT_NUMBER);
//        context.startService(s);
//    }
//}
