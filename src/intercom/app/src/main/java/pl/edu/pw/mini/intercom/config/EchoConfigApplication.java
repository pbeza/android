package pl.edu.pw.mini.intercom.config;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import pl.edu.pw.mini.intercom.R;
import pl.edu.pw.mini.intercom.gui.MainActivity;

/*
 * See:
 *   http://stackoverflow.com/questions/456211/activity-restart-on-rotation-android
 *   http://stackoverflow.com/questions/3826905/singletons-vs-application-context-in-android
 *   http://www.devahead.com/blog/2011/06/extending-the-android-application-class-and-dealing-with-singleton/
 *   https://developer.android.com/reference/android/app/Application.html (see 'Note')
 */
public class EchoConfigApplication extends Application {

    private static final String LOG_TAG = "EchoConfigApplication";

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private int mId = 1;

    public void NotifyWhenExitingWithRunningService() {
        if (!LifecycleHandler.isApplicationInForeground()) {
            Log.w("LifecycleHandler", "whole application goes to background");

            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getResources().getString(R.string.notification_title))
                    .setContentText(getResources().getString(R.string.notification_text))
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setOngoing(true);

            Intent resultIntent = new Intent(this, MainActivity.class);
            // The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

// mId allows you to update the notification later on.
            mNotificationManager.notify(mId, mBuilder.build());


        }
    }

    public void CancelNotificationOnStartingAnyActivity() {
        mNotificationManager.cancelAll();
    }
//    private final Handler messageQueueHandler = new EchoServiceMessageQueueHandler();
//    private EchoService echoService;
//    private final ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            echoService = ((EchoService.EchoServiceBinder) service).getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName className) {
//            echoService = null;
//        }
//    };
//
//    public boolean isEchoServiceBinded() {
//        return echoService != null;
//    }
//
//    public boolean bindEchoService() {
//        Intent intent = new Intent(this, EchoService.class);
//        // Messenger for the communication back from the Service to the Activity
//        Messenger messenger = new Messenger(messageQueueHandler);
//        intent.putExtra(EchoService.EXTRAS_MESSENGER_PARAM, messenger);
////        intent.setAction(EchoService.ACTION_START_ECHO_PARAM);
//        return bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    public void unbindEchoService() {
//        unbindService(serviceConnection);
//        echoService = null;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new LifecycleHandler());
        mBuilder =
                new NotificationCompat.Builder(this);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        wifiConfig = WifiConfig.getInstance();
//        wifiConfig.discoverPeers();
//        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
//        EchoService.startEchoService(this);
    }

//    public EchoService getEchoService() {
//        return echoService;
//    }


}

