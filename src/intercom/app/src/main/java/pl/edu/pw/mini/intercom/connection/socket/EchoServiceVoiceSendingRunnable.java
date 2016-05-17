package pl.edu.pw.mini.intercom.connection.socket;

import android.media.AudioRecord;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public class EchoServiceVoiceSendingRunnable extends EchoServiceBaseRunnable {

    private static final String LOG_TAG = "RecordSendVoiceRunnable";

    private EchoServiceVoiceSendingRunnable(AudioConfig audioConfig, boolean amIGroupOwner, String peerHost) {
        super(audioConfig, amIGroupOwner, peerHost);
    }

    public static void startSendingRunnable(boolean amIGroupOwner, String serverHost, AudioConfig audioConfig) {
        Runnable sendingRunnable = new EchoServiceVoiceSendingRunnable(audioConfig, amIGroupOwner, serverHost);
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
        byte[] audioData = new byte[audioConfig.minAudioRecordBufferInBytes];
        DatagramSocket peerSocket = new DatagramSocket();
        InetAddress peerAddress = InetAddress.getByName(peerHost);
        try {
            audioConfig.audioRecord.startRecording();
        } catch (IllegalStateException e) {
            Log.d(LOG_TAG, "audioRecord.startRecording() has failed", e);
            throw e;
        }
        final int byteOffset = 0;
        while (!stopRunnable) {
            int readBytes = audioConfig.audioRecord.read(audioData, byteOffset, audioConfig.minAudioRecordBufferInBytes);
            assert readBytes != AudioRecord.ERROR_BAD_VALUE;
            DatagramPacket p = new DatagramPacket(audioData, readBytes, peerAddress, PORT);
            peerSocket.send(p); // carelessly sends to peer even if not listening (beauty of UDP) :)
            //audioConfig.audioTrack.write(audioData, byteOffset, readBytes); // this is blocking write (WRITE_BLOCKING)
        }
        peerSocket.close();
    }
}
