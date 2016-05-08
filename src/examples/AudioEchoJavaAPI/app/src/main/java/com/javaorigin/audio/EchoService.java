package com.javaorigin.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

/*
 * See:
 * - http://developer.android.com/training/best-background.html
 * - https://tjakubowski.wordpress.com/2012/07/24/android-communication-between-activity-and-service/
 * - http://stackoverflow.com/questions/20594936/communication-between-activity-and-service
 * - http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
 * - http://stackoverflow.com/questions/15524280/service-vs-intentservice
 * - http://techtej.blogspot.com.es/2011/03/android-thread-constructspart-4.html
 * - http://stackoverflow.com/questions/8341667/bind-unbind-service-example-android
 */
public class EchoService extends Service {

    private final static int
            AUDIO_TRACK_MODE = AudioTrack.MODE_STREAM,
            AUDIO_RECORD_SAMPLE_RATE_IN_HZ = 32000,
            AUDIO_TRACK_SAMPLE_RATE_IN_HZ = AUDIO_RECORD_SAMPLE_RATE_IN_HZ,
            IN_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO,
            OUT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO,
            IN_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT, // guaranteed to be supported by devices TODO: choose best format
            OUT_AUDIO_FORMAT = IN_AUDIO_FORMAT;
    private final static String
            EXTRA_PARAM = "com.javaorigin.audio.extra.PARAM",
            LOG_TAG = "EchoService";
    public final static String
            ACTION_START_ECHO_PARAM = "com.javaorigin.audio.action.START_ECHO",
            MESSENGER_PARAM = "com.javaorigin.audio.extra.MESSENGER";
    private final boolean allowRebind = false;
    private final IBinder mBinder = new EchoServiceBinder();
    private AudioManager audioManager;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private Messenger outMessenger;
    private boolean
            isSpeaker = false,
            isPlaying = true;
    private int
            minAudioRecordBufferInBytes,
            minAudioTrackBufferInBytes,
            audioDataBufferLength;

    public class EchoServiceBinder extends Binder {
        EchoService getService() {
            Log.d(LOG_TAG, "EchoServiceBinder getService");

            return EchoService.this;
        }
    }

    public static void startActionEcho(Context context, String param) {
        Log.d(LOG_TAG, "startActionEcho");
        final Intent intent = new Intent(context, EchoService.class);
        intent.setAction(ACTION_START_ECHO_PARAM);
        intent.putExtra(EXTRA_PARAM, param);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");

        minAudioRecordBufferInBytes = AudioRecord.getMinBufferSize(AUDIO_RECORD_SAMPLE_RATE_IN_HZ, IN_CHANNEL_CONFIG, IN_AUDIO_FORMAT);
        audioDataBufferLength = minAudioRecordBufferInBytes * 4;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, AUDIO_RECORD_SAMPLE_RATE_IN_HZ, IN_CHANNEL_CONFIG, IN_AUDIO_FORMAT, audioDataBufferLength/*minAudioRecordBufferInBytes*/);

        minAudioTrackBufferInBytes = AudioTrack.getMinBufferSize(AUDIO_TRACK_SAMPLE_RATE_IN_HZ, OUT_CHANNEL_CONFIG, OUT_AUDIO_FORMAT);
        audioTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, AUDIO_TRACK_SAMPLE_RATE_IN_HZ, OUT_CHANNEL_CONFIG, OUT_AUDIO_FORMAT, minAudioTrackBufferInBytes, AUDIO_TRACK_MODE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        if (intent == null) {
            Log.w(LOG_TAG, "onHandleIntent intent is null");
        } else {
            final String action = intent.getAction(), param = intent.getStringExtra(EXTRA_PARAM);
            if (ACTION_START_ECHO_PARAM.equals(action)) {
                final RecordAndPlayRunnable r = new RecordAndPlayRunnable();
                new Thread(r).start();
            } else {
                final String errMsg = "Unexpected action: " + action;
                Log.e(LOG_TAG, errMsg);
                throw new RuntimeException(errMsg);
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(LOG_TAG, "onBind with extra");
            // unused, but can be used to communicate with MainActivity
            outMessenger = (Messenger) extras.get(MESSENGER_PARAM);
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        Log.d(LOG_TAG, "onUnbind");
        return allowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        super.onRebind(intent);
        Log.d(LOG_TAG, "onRebind");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    public boolean toggleSpeakerphone() {
        isSpeaker = !isSpeaker;
        audioManager.setSpeakerphoneOn(isSpeaker);
        return isSpeaker;
    }

    public boolean toggleRecording() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            audioRecord.startRecording();
            audioTrack.play();
        } else {
            audioRecord.stop();
            audioTrack.pause();
        }
        return isPlaying;
    }

    public boolean isSpeakerphoneModeOn() {
        return isSpeaker;
    }

    /* Read more - AsyncTask vs Thread vs Service vs Handler:
     * - http://stackoverflow.com/questions/6964011/handler-vs-asynctask-vs-thread
     * - http://stackoverflow.com/questions/12797550/android-asynctask-for-long-running-operations
     * - https://blog.nikitaog.me/2014/10/11/android-looper-handler-handlerthread-i/
     * - http://stackoverflow.com/questions/3264383/difference-between-service-async-task-thread
     * - http://stackoverflow.com/questions/2633334/application-threads-vs-service-threads
     * - http://stackoverflow.com/questions/7935151/difference-between-android-application-spawning-thread-vs-service
     */
    private class RecordAndPlayRunnable implements Runnable {

        private static final String LOG_TAG = "RecordAndPlayRunnable";

        @Override
        public void run() {
            Log.d(LOG_TAG, "running runnable");
            recordAndPlay();
        }

        private void recordAndPlay() {
            Log.d(LOG_TAG, "recordAndPlay");
            final short[] audioData = new short[audioDataBufferLength];
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioRecord.startRecording();
            audioTrack.play();
            while (true) {
                final int readOffsetInShorts = 0,
                        writeOffsetInShorts = 0,
                        readSizeInShorts = audioDataBufferLength, // 1024
                        writeSizeInShorts = audioRecord.read(audioData, readOffsetInShorts, readSizeInShorts);
                audioTrack.write(audioData, writeOffsetInShorts, writeSizeInShorts); // this is blocking write (WRITE_BLOCKING)
                //stopSelf();
            }
        }
    }
}
