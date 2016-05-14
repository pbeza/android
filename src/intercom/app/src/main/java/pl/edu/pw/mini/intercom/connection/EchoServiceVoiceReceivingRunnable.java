package pl.edu.pw.mini.intercom.connection;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public class EchoServiceVoiceReceivingRunnable extends EchoServiceBaseRunnable {

    private static final String LOG_TAG = "VoiceReceivingRunnable";
    private static final int BYTE_OFFSET = 0;

    public EchoServiceVoiceReceivingRunnable(AudioConfig audioConfig, boolean amIGroupOwner, Socket peerConnection) {
        super(audioConfig, amIGroupOwner, peerConnection);
    }

    @Override
    public void run() {
        assertNotMainThread();
        Log.d(LOG_TAG, "Runnable receiving voice from peer was successfully started");
        try {
            receiveVoiceFromPeer(peerSocket);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception caught during receiving voice to peer");
        }
    }

    private void receiveVoiceFromPeer(Socket peerSocket) throws IOException {
        byte[] audioData = new byte[audioConfig.minAudioRecordBufferInBytes];
        audioConfig.audioTrack.play(); // TODO probably need to synchronize parallel access
        InputStream is = peerSocket.getInputStream();
        while (!stopRunnable) {
            int bytesRead = is.read(audioData, BYTE_OFFSET, audioConfig.minAudioRecordBufferInBytes);
            Log.v(LOG_TAG, bytesRead == -1 ? "End of input stream" : bytesRead + " bytes read");
        }
        is.close();
    }
}
