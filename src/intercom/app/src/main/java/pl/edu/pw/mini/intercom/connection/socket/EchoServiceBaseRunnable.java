package pl.edu.pw.mini.intercom.connection.socket;

import android.os.Looper;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public abstract class EchoServiceBaseRunnable implements Runnable {

    public final static int PORT = 8988;
    protected final AudioConfig audioConfig;
    protected final boolean amIGroupOwner;
    protected final String peerHost;
    protected volatile boolean stopRunnable = false;

    protected EchoServiceBaseRunnable(AudioConfig audioConfig, boolean amIGroupOwner, String peerHost) {
        this.audioConfig = audioConfig;
        this.amIGroupOwner = amIGroupOwner;
        this.peerHost = peerHost;
    }

    public synchronized void stop() {// TODO synchronized needed?
        stopRunnable = true;
    }

    protected static void startRunnable(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    protected void assertNotMainThread() {
        // See: http://stackoverflow.com/questions/11411022/how-to-check-if-current-thread-is-not-main-thread
        assert Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    public void run() {
        assertNotMainThread();
    }
}
