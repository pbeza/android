package pl.edu.pw.mini.intercom.connection;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public class EchoServiceVoiceSendingRunnable extends EchoServiceBaseRunnable {

    private static final String LOG_TAG = "RecordSendVoiceRunnable";
    private static final int BYTE_OFFSET = 0;

    public EchoServiceVoiceSendingRunnable(AudioConfig audioConfig, boolean amIGroupOwner, Socket peerConnection) {
        super(audioConfig, amIGroupOwner, peerConnection);
    }

    @Override
    public void run() {
        assertNotMainThread();
        Log.d(LOG_TAG, "Runnable recording and sending voice to peer was successfully started");
        try {
            sendVoiceToPeer(peerSocket);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Exception caught during recording and sending voice to peer");
        }
    }

    private void sendVoiceToPeer(Socket peerSocket) throws IOException {
        byte[] audioData = new byte[audioConfig.minAudioRecordBufferInBytes];
        audioConfig.audioRecord.startRecording();
        OutputStream os = peerSocket.getOutputStream();
        while (!stopRunnable) {
            int readBytes = audioConfig.audioRecord.read(audioData, BYTE_OFFSET, audioConfig.minAudioRecordBufferInBytes);
            os.write(audioData, BYTE_OFFSET, readBytes); // write to peer
            audioConfig.audioTrack.write(audioData, BYTE_OFFSET, readBytes); // this is blocking write (WRITE_BLOCKING)
        }
        os.close();
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
