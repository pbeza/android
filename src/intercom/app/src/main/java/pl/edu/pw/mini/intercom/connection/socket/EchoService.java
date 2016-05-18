package pl.edu.pw.mini.intercom.connection.socket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import pl.edu.pw.mini.intercom.audio.AudioConfig;

public class EchoService extends Service {

    private final static String
            LOG_TAG = "EchoService",
            PACKAGE_PREFIX = "pl.edu.pw.mini.intercom.",
            EXTRAS_AM_I_GROUP_OWNER = PACKAGE_PREFIX + "AmIGroupOwner",
            EXTRAS_GROUP_OWNER_ADDRESS = PACKAGE_PREFIX + "GroupOwnerAddress",
            ACTION_START_ECHO_INTENT = PACKAGE_PREFIX + "START_ECHO",
            ACTION_STOP_ECHO_INTENT = PACKAGE_PREFIX + "STOP_ECHO";
    public final static String EXTRAS_MESSENGER_PARAM = PACKAGE_PREFIX + "Messenger";
    private final static boolean ALLOW_REBIND = false;
    private final IBinder echoServiceBinder = new EchoServiceBinder();
    private AudioConfig audioConfig;
    private Messenger outMessenger;

    public class EchoServiceBinder extends Binder {
        public EchoService getService() {
            return EchoService.this;
        }
    }

    private class IntentExtraParams {
        public boolean amIGroupOwner;
        public String serverHost;
    }

    public static void startEchoService(Context context, boolean isGroupOwner, String groupOwnerHost) {
        Intent intent = new Intent(context, EchoService.class);
        intent.setAction(ACTION_START_ECHO_INTENT);
        intent.putExtra(EXTRAS_AM_I_GROUP_OWNER, isGroupOwner);
        intent.putExtra(EXTRAS_GROUP_OWNER_ADDRESS, groupOwnerHost);
        context.startService(intent);
    }

    public static void stopEchoService(Context context) {
        Intent intent = new Intent(context, EchoService.class);
        intent.setAction(ACTION_STOP_ECHO_INTENT);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        Context context = getApplicationContext();
        audioConfig = new AudioConfig(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.w(LOG_TAG, "onStartCommand intent is null");
        } else {
            assertPresenceOfAllRequiredExtraParams(intent);
            IntentExtraParams intentExtraParams = parseExtraParams(intent);
            runActionBasedOnStartCommandIntent(intent, intentExtraParams);
        }
        return START_STICKY;
    }

    private void assertPresenceOfAllRequiredExtraParams(Intent intent) {
        String[] requiredParams;
        String action = intent.getAction();
        switch (action) {
            case ACTION_START_ECHO_INTENT:
                requiredParams = new String[]{
                        EXTRAS_GROUP_OWNER_ADDRESS,
                        EXTRAS_AM_I_GROUP_OWNER
                };
                break;
            case ACTION_STOP_ECHO_INTENT:
                requiredParams = new String[]{};
                break;
            default:
                throw new RuntimeException("Unexpected action: " + action);
        }
        assertPresenceOfAllRequiredExtraParams(intent, requiredParams);
    }

    private void assertPresenceOfAllRequiredExtraParams(Intent intent, String[] requiredParams) {
        for (String p : requiredParams) {
            boolean present = intent.hasExtra(p);
            if (!present) {
                Log.e(LOG_TAG, "Required " + p + " intent extra param not found");
            }
        }
    }

    private IntentExtraParams parseExtraParams(Intent intent) {
        IntentExtraParams p = new IntentExtraParams();
        final boolean amIGroupOwnerDefaultValue = false;
        p.serverHost = intent.getStringExtra(EXTRAS_GROUP_OWNER_ADDRESS);
        p.amIGroupOwner = intent.getBooleanExtra(EXTRAS_AM_I_GROUP_OWNER, amIGroupOwnerDefaultValue);
        return p;
    }

    private void runActionBasedOnStartCommandIntent(Intent intent, IntentExtraParams intentExtraParams) {
        String action = intent.getAction();
        switch (action) {
            case ACTION_START_ECHO_INTENT:
                startSocketConnection(intentExtraParams);
                break;
            case ACTION_STOP_ECHO_INTENT:
                // TODO
                break;
            default:
                throw new RuntimeException("Unexpected action: " + action);
        }
    }

    private void startSocketConnection(IntentExtraParams intentExtraParams) {
        // We are already connected with peer via WiFi peer2peer, so that we can open socket connection
        EchoServiceVoiceReceivingRunnable.startReceivingRunnable(intentExtraParams.amIGroupOwner, intentExtraParams.serverHost, audioConfig);
        if (!intentExtraParams.amIGroupOwner) {
            EchoServiceVoiceSendingRunnable.startSendingRunnable(false, intentExtraParams.serverHost, audioConfig);
        } // else start in receiving runnable when you receive first packet from client
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            // Messenger can be used to communicate with MainActivity
            outMessenger = (Messenger) extras.get(EXTRAS_MESSENGER_PARAM);
        }
        return echoServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        return ALLOW_REBIND;
    }

//    @Override
//    public void onRebind(Intent intent) {
//        // A client is binding to the service with bindService() after onUnbind() has already been called
//        super.onRebind(intent);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
}
