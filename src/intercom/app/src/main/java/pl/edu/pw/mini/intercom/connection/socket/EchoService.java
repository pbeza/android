package pl.edu.pw.mini.intercom.connection.socket;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class EchoService extends Service {

    private final static String
            LOG_TAG = "EchoService",
            PACKAGE_PREFIX = "pl.edu.pw.mini.intercom.",
            EXTRAS_AM_I_GROUP_OWNER = PACKAGE_PREFIX + "AmIGroupOwner",
            EXTRAS_GROUP_OWNER_ADDRESS = PACKAGE_PREFIX + "GroupOwnerAddress",
    ACTION_START_ECHO_INTENT = PACKAGE_PREFIX + "START_ECHO",
            ACTION_STOP_ECHO_INTENT = PACKAGE_PREFIX + "STOP_ECHO";
    private final static String[]
            ACTION_START_ECHO_INTENT_REQUIRED_PARAMS = new String[]{ EXTRAS_AM_I_GROUP_OWNER, EXTRAS_GROUP_OWNER_ADDRESS },
            ACTION_STOP_ECHO_INTENT_REQUIRED_PARAMS = new String[]{};
    public final static String EXTRAS_MESSENGER_PARAM = PACKAGE_PREFIX + "Messenger";
    private final static boolean ALLOW_REBIND = false;
    private final IBinder echoServiceBinder = new EchoServiceBinder();
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
        super.onCreate(); // TODO anything todo here?
    }

    /*
     * Will be called many times.
     * See: http://stackoverflow.com/questions/10739413/how-to-prevent-service-to-run-again-if-already-running-android
     */
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
                requiredParams = ACTION_START_ECHO_INTENT_REQUIRED_PARAMS;
                break;
            case ACTION_STOP_ECHO_INTENT:
                requiredParams = ACTION_STOP_ECHO_INTENT_REQUIRED_PARAMS;
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
                throw new AssertionError("Required " + p + " intent extra param not found");
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
                startSocketConnection(intentExtraParams.amIGroupOwner, intentExtraParams.serverHost);
                break;
            case ACTION_STOP_ECHO_INTENT:
                break;
            default:
                throw new RuntimeException("Unexpected action: " + action);
        }
    }

    public void startSocketConnection(boolean amIGroupOwner, String serverHostAddress) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // We are already connected with peer via WiFi peer2peer, so that we can open socket connection
        EchoServiceVoiceReceivingRunnable.startReceivingRunnable(audioManager, amIGroupOwner, serverHostAddress);
        if (!amIGroupOwner) {
            EchoServiceVoiceSendingRunnable.startSendingRunnable(audioManager, false, serverHostAddress);
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
}
