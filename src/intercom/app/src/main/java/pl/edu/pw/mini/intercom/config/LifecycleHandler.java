package pl.edu.pw.mini.intercom.config;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by ppp on 2016-05-30.
 */
public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {

    private static int resumed;
    private static int stopped;

    public static boolean isApplicationInForeground() {
        return resumed > stopped;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        ((EchoConfigApplication) activity.getApplication()).CancelNotificationOnStartingAnyActivity();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;
        android.util.Log.w("test", "application is being backgrounded: " + (resumed == stopped));
        ((EchoConfigApplication) activity.getApplication()).NotifyWhenExitingWithRunningService();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


}
