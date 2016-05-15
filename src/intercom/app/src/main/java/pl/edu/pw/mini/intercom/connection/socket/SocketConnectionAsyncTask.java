package pl.edu.pw.mini.intercom.connection.socket;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public class SocketConnectionAsyncTask extends AsyncTask<Void, Void, Socket> {

    private static final String LOG_TAG = "ConnectionManagerAsync";
    protected final static int
            SERVER_PORT = 8988,
            CLIENT_CONNECTION_TO_SERVER_TIMEOUT_IN_MILLISECONDS = 10000;
    protected Socket peerSocket;
    protected AudioConfig audioConfig;
    protected final boolean amIGroupOwner;
    protected final String serverHost;

    public SocketConnectionAsyncTask(AudioConfig audioConfig, boolean amIGroupOwner, String serverHost) {
        this.audioConfig = audioConfig;
        this.amIGroupOwner = amIGroupOwner;
        this.serverHost = serverHost;
    }

    @Override
    protected Socket doInBackground(Void... params) {
        try {
            return peerSocket = initSocket();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Connection initialization has failed");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Socket result) {
        startRecordingRunnable();
        startListeningRunnable();
    }

    public void closeConnection() throws IOException {
        peerSocket.close();
    }

    private Socket initSocket() throws IOException {
        Socket socket;
        try {
            socket = amIGroupOwner ? initServerSocket() : initClientSocket();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error during "
                    + (amIGroupOwner ? "server" : "client")
                    + " socket initialization for "
                    + serverHost + ":" + SERVER_PORT, e);
            throw e;
        }
        return socket;
    }

    private Socket initServerSocket() throws IOException {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            Log.d(LOG_TAG, "Server's socket opened for clients");
            clientSocket = serverSocket.accept();
            Log.d(LOG_TAG, "Server accepted connection from clientSocket");
        } catch (IOException e) {
            clientSocket.close();
            throw e;
        } finally {
            serverSocket.close();
        }
        Log.d(LOG_TAG, "Server's socket closed for new clients");
        return clientSocket;
    }

    private Socket initClientSocket() throws IOException {
        Socket clientSocket;
        try {
            clientSocket = new Socket(serverHost, SERVER_PORT);
            if (clientSocket.isBound()) {
                Log.wtf(LOG_TAG, "Client's socket is already bound");
            } else {
                clientSocket.bind(null);
                Log.d(LOG_TAG, "Client's socket successfully bound");
            }
            if (clientSocket.isConnected()) {
                Log.wtf(LOG_TAG, "Client's socket is already connected");
            } else {
                InetSocketAddress serverSocketAddress = new InetSocketAddress(serverHost, SERVER_PORT);
                clientSocket.connect(serverSocketAddress, CLIENT_CONNECTION_TO_SERVER_TIMEOUT_IN_MILLISECONDS);
                Log.d(LOG_TAG, "Client's socket successfully connected");
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Server was not started yet. Start server first.");
            throw e;
        }
        Log.d(LOG_TAG, "Client: socket isConnected = " + clientSocket.isConnected());
        return clientSocket;
    }

    private void startRecordingRunnable() {
        EchoServiceVoiceSendingRunnable r1 = new EchoServiceVoiceSendingRunnable(audioConfig, amIGroupOwner, peerSocket);
        Thread t1 = new Thread(r1);
        t1.start();
        Log.d(LOG_TAG, "Thread recording and sending voice to peer started successfully");
    }

    private void startListeningRunnable() {
        EchoServiceVoiceReceivingRunnable r2 = new EchoServiceVoiceReceivingRunnable(audioConfig, amIGroupOwner, peerSocket);
        Thread t2 = new Thread(r2);
        t2.start();
        Log.d(LOG_TAG, "Thread listening peer started successfully");
    }
}
