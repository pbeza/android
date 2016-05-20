package pl.edu.pw.mini.intercom.config;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

import pl.edu.pw.mini.intercom.audio.AudioConfig;
import pl.edu.pw.mini.intercom.connection.p2p.WifiConfig;
import pl.edu.pw.mini.intercom.connection.socket.EchoService;
import pl.edu.pw.mini.intercom.connection.socket.EchoServiceMessageQueueHandler;
import pl.edu.pw.mini.intercom.gui.MainActivity;

/*
 * See:
 *   http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
 *   http://stackoverflow.com/questions/3826905/singletons-vs-application-context-in-android
 *   http://www.devahead.com/blog/2011/06/extending-the-android-application-class-and-dealing-with-singleton/
 */
public class EchoConfigApplication extends Application {

    private final Handler messageQueueHandler = new EchoServiceMessageQueueHandler();
    private WifiConfig wifiConfig;
    private AudioConfig audioConfig;
    private EchoService echoService;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            echoService = ((EchoService.EchoServiceBinder) service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName className) {
            echoService = null;
        }
    };

    public void updateMainActivityReference(MainActivity mainActivity) {
        wifiConfig.updateContextReferences(mainActivity);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioConfig.updateAudioManagerReferences(audioManager); // TODO needed?
    }

    public boolean isEchoServiceBinded() {
        return echoService != null;
    }

    public boolean bindEchoService() {
        Intent intent = new Intent(this, EchoService.class);
        // Messenger for the communication back from the Service to the Activity
        Messenger messenger = new Messenger(messageQueueHandler);
        intent.putExtra(EchoService.EXTRAS_MESSENGER_PARAM, messenger);
//        intent.setAction(EchoService.ACTION_START_ECHO_PARAM);
        return bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindEchoService() {
        unbindService(serviceConnection);
        echoService = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioConfig = AudioConfig.getInstance();
        wifiConfig = WifiConfig.getInstance();
//        wifiConfig.discoverPeers();
//        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        EchoService.startEchoService(this);
    }

    public EchoService getEchoService() {
        return echoService;
    }

}
