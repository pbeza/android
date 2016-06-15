package pl.edu.pw.mini.intercom.connection.socket;

import android.media.AudioManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EchoServiceVoiceSendingRunnable extends EchoServiceBaseRunnable {

    private static final String LOG_TAG = "RecordSendVoiceRunnable";

    private EchoServiceVoiceSendingRunnable(AudioManager audioManager, boolean amIGroupOwner, String peerHost) {
        super(audioManager, amIGroupOwner, peerHost);
    }

    public static void startSendingRunnable(AudioManager audioManager, boolean amIGroupOwner, String serverHost) {
        Runnable sendingRunnable = new EchoServiceVoiceSendingRunnable(audioManager, amIGroupOwner, serverHost);
        startRunnable(sendingRunnable);
        Log.d(LOG_TAG, "Thread recording and sending voice to peer started successfully");
    }

    @Override
    public void run() {
        super.run();
        Log.d(LOG_TAG, "Runnable recording and sending voice to peer successfully started");
        try {
            sendVoiceToPeer();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception caught during recording and sending voice to peer");
        }
    }

    private void sendVoiceToPeer() throws IOException {
        final int audioRecordBufferInBytes = audioConfig.getAudioRecordBufferInBytes();
        byte[] audioData = new byte[audioRecordBufferInBytes];
        DatagramSocket peerSocket = new DatagramSocket();
        InetAddress peerAddress = InetAddress.getByName(peerHost);
        try {
            audioConfig.startRecording();
        } catch (IllegalStateException e) {
            audioConfig.release();
            Log.d(LOG_TAG, "audioRecord.startRecording() has failed", e);
            throw e;
        }
        while (!stopRunnable) {
            int readBytes = audioConfig.read(audioData);
            DatagramPacket p = new DatagramPacket(audioData, readBytes, peerAddress, PORT);
            peerSocket.send(p); // careleussly sends to peer even if not listening (beauty of UDP) :)
            //audioConfig.write(audioData, byteOffset, readBytes); // uncomment to hear your voice in speaker
        }
        peerSocket.close();
    }
}
