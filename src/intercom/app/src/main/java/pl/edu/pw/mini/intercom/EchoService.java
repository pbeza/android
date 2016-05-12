package pl.edu.pw.mini.intercom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoService extends Service {

    private final static String
            LOG_TAG = "EchoService",
            PACKAGE_PREFIX = "pl.edu.pw.mini.intercom.",
            EXTRAS_AM_I_GROUP_OWNER = PACKAGE_PREFIX + "AmIGroupOwner",
            EXTRAS_GROUP_OWNER_ADDRESS = PACKAGE_PREFIX + "GroupOwnerAddress",
            ACTION_START_ECHO_INTENT = PACKAGE_PREFIX + "START_ECHO",
            ACTION_STOP_ECHO_INTENT = PACKAGE_PREFIX + "STOP_ECHO";
    public final static String EXTRAS_MESSENGER_PARAM = PACKAGE_PREFIX + "Messenger";
    private final static int
            AUDIO_TRACK_MODE = AudioTrack.MODE_STREAM,
            AUDIO_RECORD_SAMPLE_RATE_IN_HZ = 32000,
            AUDIO_TRACK_SAMPLE_RATE_IN_HZ = AUDIO_RECORD_SAMPLE_RATE_IN_HZ,
            IN_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO,
            OUT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO,
            IN_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT, // TODO switch to 8BIT
            OUT_AUDIO_FORMAT = IN_AUDIO_FORMAT,
            SERVER_PORT = 8988,
            SOCKET_TIMEOUT_IN_MILLISECONDS = 5000;
    private final static boolean allowRebind = false;
    private final IBinder mBinder = new EchoServiceBinder();
    private AudioManager audioManager;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Messenger outMessenger;
    private boolean
            isSpeaker = false,
            isPlaying = true;
    private volatile boolean stopRunnable = false;
    private int
            minAudioRecordBufferInBytes,
            minAudioTrackBufferInBytes,
            audioRecordDataBufferLengthInBytes;
    private String host;
    private boolean amIGroupOwner;

    public class EchoServiceBinder extends Binder {
        EchoService getService() {
            return EchoService.this;
        }
    }

    private class RecordAndSendVoiceRunnable implements Runnable {

        private static final String LOG_TAG = "RecordSendVoiceRunnable";

        @Override
        public void run() {
            try {
                final Socket socket = initSocket();
                recordPlayAndSendVoiceToPeer(socket);
                socket.close();
            } catch (IOException e) {
                Log.d(LOG_TAG, "Reading, writing or closing peer's socket has failed", e);
            }
            Log.d(LOG_TAG, "Runnable ended successfully");
            //stopSelf();
        }

        private Socket initSocket() throws IOException {
            Socket socket;
            try {
                if (amIGroupOwner) {
                    socket = initServerSocket();
                } else {
                    socket = initClientSocket();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error during socket initialization for " + host + ":" + SERVER_PORT, e);
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
                Log.d(LOG_TAG, "Server's socket closed for new clients");
            }
            return clientSocket;
        }

        private Socket initClientSocket() throws IOException {
            Socket clientSocket;
            try {
                clientSocket = new Socket(host, SERVER_PORT);
                if (clientSocket.isBound()) {
                    Log.wtf(LOG_TAG, "Client's socket is already bound");
                } else {
                    clientSocket.bind(null);
                }
                if (clientSocket.isConnected()) {
                    Log.d(LOG_TAG, "Client's socket is already connected");
                } else {
                    clientSocket.connect(new InetSocketAddress(host, SERVER_PORT), SOCKET_TIMEOUT_IN_MILLISECONDS);
                }
                Log.d(LOG_TAG, "Client: socket isConnected = " + clientSocket.isConnected());
            } catch (IOException e) {
                Log.d(LOG_TAG, "Server was not started yet. Start server first.");
                throw e;
            }
            return clientSocket;
        }

        private void recordPlayAndSendVoiceToPeer(Socket socket) throws IOException {
            final byte[] audioDataInBytes = new byte[audioRecordDataBufferLengthInBytes];
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioRecord.startRecording();
            audioTrack.play();
            final OutputStream os = socket.getOutputStream();
            final InputStream is = socket.getInputStream();
            while (!stopRunnable) {
                final int byteOffset = 0, readBytes = audioRecord.read(audioDataInBytes, byteOffset, audioRecordDataBufferLengthInBytes);
                try {
                    os.write(audioDataInBytes, byteOffset, readBytes); // write to peer
                    is.read(audioDataInBytes, byteOffset, audioRecordDataBufferLengthInBytes); // read from peer
                    audioTrack.write(audioDataInBytes, byteOffset, readBytes); // this is blocking write (WRITE_BLOCKING)
                } catch (IOException e) {
                    throw e;
                }
            }
            os.close();
            is.close();
        }

//        public static boolean copyFile(InputStream inputStream, OutputStream out) {
//            final int bufSize = 1024;
//            final byte buf[] = new byte[bufSize];
//            int len;
//            try {
//                final int offset = 0;
//                while ((len = inputStream.read(buf)) != -1) {
//                    out.write(buf, offset, len);
//                }
//                out.close();
//                inputStream.close();
//            } catch (IOException e) {
//                Log.e(LOG_TAG, e.toString(), e);
//                return false;
//            }
//            return true;
//        }
    }

    public static void startEchoService(Context context, boolean isGroupOwner, String groupOwnerAddr) {
        final Intent intent = new Intent(context, EchoService.class);
        intent.setAction(ACTION_START_ECHO_INTENT);
        intent.putExtra(EXTRAS_AM_I_GROUP_OWNER, isGroupOwner);
        intent.putExtra(EXTRAS_GROUP_OWNER_ADDRESS, groupOwnerAddr);
        context.startService(intent);
    }

    public static void stopEchoService(Context context) {
        final Intent intent = new Intent(context, EchoService.class);
        intent.setAction(ACTION_STOP_ECHO_INTENT);
        context.stopService(intent);
    }

    public boolean toggleSpeakerphone() {
        isSpeaker = !isSpeaker;
        audioManager.setSpeakerphoneOn(isSpeaker);
        return isSpeaker;
    }

    public boolean toggleRecording() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            audioRecord.startRecording();
            audioTrack.play();
        } else {
            audioRecord.stop();
            audioTrack.pause();
        }
        return isPlaying;
    }

    public boolean isSpeakerphoneModeOn() {
        return isSpeaker;
    }

    @Override
    public void onCreate() {
        minAudioRecordBufferInBytes = AudioRecord.getMinBufferSize(AUDIO_RECORD_SAMPLE_RATE_IN_HZ, IN_CHANNEL_CONFIG, IN_AUDIO_FORMAT);
        audioRecordDataBufferLengthInBytes = minAudioRecordBufferInBytes * 4;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, AUDIO_RECORD_SAMPLE_RATE_IN_HZ, IN_CHANNEL_CONFIG, IN_AUDIO_FORMAT, /*audioRecordDataBufferLengthInBytes*/minAudioRecordBufferInBytes);
        minAudioTrackBufferInBytes = AudioTrack.getMinBufferSize(AUDIO_TRACK_SAMPLE_RATE_IN_HZ, OUT_CHANNEL_CONFIG, OUT_AUDIO_FORMAT);
        audioTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, AUDIO_TRACK_SAMPLE_RATE_IN_HZ, OUT_CHANNEL_CONFIG, OUT_AUDIO_FORMAT, minAudioTrackBufferInBytes, AUDIO_TRACK_MODE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.w(LOG_TAG, "onHandleIntent intent is null");
        } else {
            final String action = intent.getAction();
            host = intent.getStringExtra(EXTRAS_GROUP_OWNER_ADDRESS);
            amIGroupOwner = intent.getBooleanExtra(EXTRAS_AM_I_GROUP_OWNER, false);
            if (intent.hasExtra(EXTRAS_AM_I_GROUP_OWNER)) {
                Log.e(LOG_TAG, "EXTRAS_AM_I_GROUP_OWNER not found");
            }
            if (ACTION_START_ECHO_INTENT.equals(action)) {
                final RecordAndSendVoiceRunnable r = new RecordAndSendVoiceRunnable();
                new Thread(r).start();
            } else if (ACTION_START_ECHO_INTENT.equals(action)) {
                Log.d(LOG_TAG, "onStartCommand = ACTION_START_ECHO_INTENT");
                stopRunnable = true;
            } else {
                final String errMsg = "Unexpected action: " + action;
                final RuntimeException runtimeException = new RuntimeException(errMsg);
                Log.e(LOG_TAG, errMsg, runtimeException);
                throw runtimeException;
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            // Messenger can be used to communicate with MainActivity
            outMessenger = (Messenger) extras.get(EXTRAS_MESSENGER_PARAM);
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        return allowRebind;
    }

//    @Override
//    public void onRebind(Intent intent) {
//        // A client is binding to the service with bindService() after onUnbind() has already been called
//        super.onRebind(intent);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
}
