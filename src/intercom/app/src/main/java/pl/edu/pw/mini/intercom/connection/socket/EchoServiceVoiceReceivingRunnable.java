package pl.edu.pw.mini.intercom.connection.socket;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EchoServiceVoiceReceivingRunnable extends EchoServiceBaseRunnable {

    private static final String LOG_TAG = "VoiceReceivingRunnable";

    private EchoServiceVoiceReceivingRunnable(boolean amIGroupOwner, String peerHost) {
        super(amIGroupOwner, peerHost);
    }

    public static void startReceivingRunnable(boolean amIGroupOwner, String serverHost) {
        Runnable receivingRunnable = new EchoServiceVoiceReceivingRunnable(amIGroupOwner, serverHost);
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
        final int audioRecordBufferInBytes = audioConfig.getAudioRecordBufferInBytes();
        byte[] audioData = new byte[audioRecordBufferInBytes];
        DatagramSocket peerSocket = new DatagramSocket(PORT);
        DatagramPacket packet = new DatagramPacket(audioData, audioData.length);
        if (amIGroupOwner) {
            startSendingRunnableOnReceivedFirstPacket(peerSocket, packet);
        }
        try {
            audioConfig.startPlaying();
        } catch (IllegalStateException e) {
            Log.d(LOG_TAG, "audioTrack.startPlaying() has failed", e);
            throw e;
        }
        while (!stopRunnable) {
            peerSocket.receive(packet); // blocking operation
            InetAddress peerAddress = packet.getAddress();
            Log.v(LOG_TAG, "Received " + packet.getLength() + " bytes from " + peerAddress.getHostAddress());
            audioConfig.write(packet);
        }
    }

    private void startSendingRunnableOnReceivedFirstPacket(DatagramSocket peerSocket, DatagramPacket packet) throws IOException {
        InetAddress peerAddress;
        String hostName;
        try {
            peerSocket.setSoTimeout(0); // wait infinitely for first packet
            Log.d(LOG_TAG, "Waiting for first packet from non-GroupOwner peer to obtain his IP address");
            peerSocket.receive(packet); // blocking operation
            peerAddress = packet.getAddress();
            hostName = peerAddress.getHostName();
            Log.d(LOG_TAG, "Received first packet from non-GroupOwner peer - his IP: " + hostName);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Didn't receive first packet", e);
            throw e;
        }
        EchoServiceVoiceSendingRunnable.startSendingRunnable(amIGroupOwner, hostName);
    }
}
