package pl.edu.pw.mini.intercom.connection;

import android.os.Looper;

import java.net.Socket;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public abstract class EchoServiceBaseRunnable implements Runnable {

    protected final AudioConfig audioConfig;
    protected final boolean amIGroupOwner;
    protected Socket peerSocket;
    protected volatile boolean stopRunnable = false;

    protected EchoServiceBaseRunnable(AudioConfig audioConfig, boolean amIGroupOwner, Socket peerSocket) {
        this.audioConfig = audioConfig;
        this.amIGroupOwner = amIGroupOwner;
        this.peerSocket = peerSocket;
    }

    public synchronized void stop() {// TODO synchronized needed?
        stopRunnable = true;
    }

    protected void assertNotMainThread() {
        // See: http://stackoverflow.com/questions/11411022/how-to-check-if-current-thread-is-not-main-thread
        assert Looper.myLooper() == Looper.getMainLooper();
    }
}
