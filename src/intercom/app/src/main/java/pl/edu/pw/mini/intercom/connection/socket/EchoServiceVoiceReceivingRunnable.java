package pl.edu.pw.mini.intercom.connection.socket;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public class EchoServiceVoiceReceivingRunnable extends EchoServiceBaseRunnable {

    private static final String LOG_TAG = "VoiceReceivingRunnable";

    public EchoServiceVoiceReceivingRunnable(AudioConfig audioConfig, boolean amIGroupOwner, String peerHost) {
        super(audioConfig, amIGroupOwner, peerHost);
    }

    public static void startReceivingRunnable(boolean amIGroupOwner, String serverHost, AudioConfig audioConfig) {
        Runnable receivingRunnable = new EchoServiceVoiceReceivingRunnable(audioConfig, amIGroupOwner, serverHost);
        startRunnable(receivingRunnable);
        Log.d(LOG_TAG, "Thread receiving voice from peer started successfully");
    }

    @Override
    public void run() {
        super.run();
        Log.d(LOG_TAG, "Runnable receiving voice from peer successfully started");
        try {
            receiveVoiceFromPeer();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception caught during receiving voice to peer");
        }
    }

    private void receiveVoiceFromPeer() throws IOException {
        byte[] audioData = new byte[audioConfig.minAudioRecordBufferInBytes];
        DatagramSocket peerSocket = new DatagramSocket(PORT);
        // TODO possibly one more thread reading received, buffered audio would be better
        DatagramPacket packet = new DatagramPacket(audioData, audioData.length);
        if (amIGroupOwner) {
            startSendingRunnableOnReceivedFirstPacket(peerSocket, packet);
        }
        try {
            audioConfig.audioTrack.play(); // TODO probably need to synchronize parallel access
        } catch (IllegalStateException e) {
            Log.d(LOG_TAG, "audioTrack.play() has failed", e);
            throw e;
        }
        final int byteOffset = 0;
        while (!stopRunnable) {
            peerSocket.receive(packet);
            InetAddress peerAddress = packet.getAddress();
            Log.v(LOG_TAG, "Received " + packet.getLength() + " bytes from " + peerAddress.getHostAddress());
            audioConfig.audioTrack.write(packet.getData(), byteOffset, packet.getLength());
        }
    }

    private void startSendingRunnableOnReceivedFirstPacket(DatagramSocket peerSocket, DatagramPacket packet) throws IOException {
        try {
            peerSocket.receive(packet);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Didn't receive first packet", e);
            throw e;
        }
        InetAddress peerAddress = packet.getAddress();
        String hostName = peerAddress.getHostName();
        EchoServiceVoiceSendingRunnable.startSendingRunnable(amIGroupOwner, hostName, audioConfig);
    }
}
