package com.javaorigin.audio;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private final static String LOG_TAG = "MainActivity";
    private final Handler msgQueueHandler = new EchoHandler();
    private EchoService echoService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(LOG_TAG, "ServiceConnection connected (className=" + className + ")");
            echoService = ((EchoService.EchoServiceBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(LOG_TAG, "ServiceConnection disconnected (className=" + className + ")");
            echoService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        EchoService.startActionEcho(this, "testParam"); // redundant, testing only param
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        if (echoService == null) {
            doBindService();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // FIXME put back (?)
        Log.d(LOG_TAG, "onPause");
        if (echoService != null) {
            unbindService(serviceConnection);
            echoService = null;
        }
        super.onPause();
    }

    public void modeChange(View view) {
        final Button modeBtn = (Button) findViewById(R.id.modeBtn);
        echoService.toggleSpeakerphone();
        final boolean isSpeakerphoneModeOn = echoService.isSpeakerphoneModeOn();
        modeBtn.setText(getString(isSpeakerphoneModeOn ? R.string.call_mode : R.string.speaker_mode));
    }

    public void play(View view) {
        final Button playBtn = (Button) findViewById(R.id.playBtn);
        final boolean isRecording = echoService.toggleRecording();
        playBtn.setText(getString(isRecording ? R.string.pause_recording : R.string.start_recording));
    }

    private void doBindService() {
        Log.d(LOG_TAG, "doBindService");
        final Intent intent = new Intent(this, EchoService.class);
        // Create a new Messenger for the communication back from the Service to the Activity
        final Messenger messenger = new Messenger(msgQueueHandler);
        intent.putExtra(EchoService.MESSENGER_PARAM, messenger);
        //intent.setAction(EchoService.ACTION_START_ECHO_PARAM);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private static class EchoHandler extends Handler {
        public void handleMessage(Message message) {
            Log.d(LOG_TAG, "EchoHandler handleMessage");
            final Bundle data = message.getData();
        }
    }
}
