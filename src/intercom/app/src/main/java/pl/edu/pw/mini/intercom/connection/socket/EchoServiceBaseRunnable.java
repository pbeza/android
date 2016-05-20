package pl.edu.pw.mini.intercom.connection.socket;

import android.os.Looper;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

abstract class EchoServiceBaseRunnable implements Runnable {

    final static int PORT = 8988;
    final AudioConfig audioConfig;
    final boolean amIGroupOwner;
    final String peerHost;
    volatile boolean stopRunnable = false;

    EchoServiceBaseRunnable(boolean amIGroupOwner, String peerHost) {
        this.audioConfig = AudioConfig.getInstance();
        this.amIGroupOwner = amIGroupOwner;
        this.peerHost = peerHost;
    }

    public synchronized void stop() {// TODO synchronized needed?
        stopRunnable = true;
    }

    static void startRunnable(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void assertNotMainThread() {
        // See: http://stackoverflow.com/questions/11411022/how-to-check-if-current-thread-is-not-main-thread
        assert Looper.myLooper() == Looper.getMainLooper();
    }

    @Override
    public void run() {
        assertNotMainThread();
    }
}
