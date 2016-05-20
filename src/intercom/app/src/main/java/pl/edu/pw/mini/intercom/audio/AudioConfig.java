package pl.edu.pw.mini.intercom.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import java.net.DatagramPacket;

import pl.edu.pw.mini.intercom.BuildConfig;

public class AudioConfig {

    // TODO adjust optimal audio parameters (sample rate, buffers length etc.)
    // TODO some method calls probably should be 'synchronized' (parallel access issues)

    private static AudioConfig instance = new AudioConfig();
    public static final int
            AUDIO_TRACK_MODE = AudioTrack.MODE_STREAM,
            AUDIO_RECORD_SAMPLE_RATE_IN_HZ = 16000,
            AUDIO_TRACK_SAMPLE_RATE_IN_HZ = AUDIO_RECORD_SAMPLE_RATE_IN_HZ,
            IN_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO,
            OUT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO,
            IN_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT,
            OUT_AUDIO_FORMAT = IN_AUDIO_FORMAT,
            AUDIO_MANAGER_COMMUNICATION_MODE = AudioManager.MODE_IN_COMMUNICATION;
    private static final boolean
            VOICE_RECORDING_ON_BY_DEFAULT = true,
            VOICE_PLAYING_ON_BY_DEFAULT = true;
    private final int audioRecordBufferInBytes, audioTrackBufferInBytes;
    private final AudioRecord audioRecord;
    private final AudioTrack audioTrack;
    private AudioManager audioManager;
    private boolean
            isVoiceRecordingOn = VOICE_RECORDING_ON_BY_DEFAULT,
            isAudioTrackPlaying = VOICE_PLAYING_ON_BY_DEFAULT;

    private AudioConfig() {
        int minAudioRecordBufferInBytes = AudioRecord.getMinBufferSize(AudioConfig.AUDIO_RECORD_SAMPLE_RATE_IN_HZ, IN_CHANNEL_CONFIG, IN_AUDIO_FORMAT);
        audioRecordBufferInBytes = minAudioRecordBufferInBytes;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, AUDIO_RECORD_SAMPLE_RATE_IN_HZ, IN_CHANNEL_CONFIG, IN_AUDIO_FORMAT, audioRecordBufferInBytes);
        int minAudioTrackBufferInBytes = AudioTrack.getMinBufferSize(AUDIO_TRACK_SAMPLE_RATE_IN_HZ, OUT_CHANNEL_CONFIG, OUT_AUDIO_FORMAT);
        audioTrackBufferInBytes = minAudioTrackBufferInBytes;
        audioTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, AUDIO_TRACK_SAMPLE_RATE_IN_HZ, OUT_CHANNEL_CONFIG, OUT_AUDIO_FORMAT, audioTrackBufferInBytes, AUDIO_TRACK_MODE);
    }

    public static AudioConfig getInstance() {
        return instance;
    }

    // TODO remove this somehow
    public void updateAudioManagerReferences(AudioManager audioManager) {
        this.audioManager = audioManager;
        audioManager.setMode(AUDIO_MANAGER_COMMUNICATION_MODE);
    }

    public int getAudioRecordBufferInBytes() {
        return audioRecordBufferInBytes;
    }

//    public int getAudioTrackBufferInBytes() {
//        return audioTrackBufferInBytes;
//    }

    public void startRecording() {
        audioRecord.startRecording();
        isVoiceRecordingOn = true;
    }

    public void stopRecording() {
        audioRecord.stop();
        isVoiceRecordingOn = false;
    }

    public void startPlaying() {
        audioTrack.play();
        isAudioTrackPlaying = true;
    }

    public void stopPlaying() {
        audioTrack.stop();
        isAudioTrackPlaying = false;
    }

    public void pausePlaying() {
        audioTrack.pause();
        audioTrack.flush();
        isAudioTrackPlaying = false;
    }

    public void setSpeakerphoneOn(boolean on) {
        audioManager.setSpeakerphoneOn(on);
    }

    public int read(byte[] audioData) {
        final int offsetInBytes = 0;
        int readBytes = audioRecord.read(audioData, offsetInBytes, audioData.length);
        if (BuildConfig.DEBUG && readBytes == AudioRecord.ERROR_BAD_VALUE) {
            throw new AssertionError("audioRecord.read() has failed");
        }
        return readBytes;
    }

    public int write(DatagramPacket datagramPacket) {
        final int offsetInBytes = 0;
        byte[] audioData = datagramPacket.getData();
        int sizeInBytes = datagramPacket.getLength();
        int wroteBytes = audioTrack.write(audioData, offsetInBytes, sizeInBytes);
        if (BuildConfig.DEBUG && (wroteBytes == AudioTrack.ERROR_INVALID_OPERATION ||
                wroteBytes == AudioTrack.ERROR_BAD_VALUE ||
                wroteBytes == AudioManager.ERROR_DEAD_OBJECT)) {
            throw new AssertionError("audioTrack.write() has failed");
        }
        return wroteBytes;
    }

    public void release() {
        if (audioManager.isSpeakerphoneOn()) {
            toggleSpeakerphone();
        }
        if (isVoiceRecordingOn) {
            stopRecording();
        }
        if (isAudioTrackPlaying) {
            stopPlaying();
        }
        audioTrack.flush();
        audioTrack.release();
        audioRecord.release();
    }

    public boolean toggleSpeakerphone() {
        final boolean isSpeakerphoneOn = !audioManager.isSpeakerphoneOn();
        setSpeakerphoneOn(isSpeakerphoneOn);
        return isSpeakerphoneOn;
    }

    public boolean toggleRecording() {
        if (isVoiceRecordingOn) {
            stopRecording();
        } else {
            startRecording();
        }
        return isVoiceRecordingOn;
    }

    public boolean togglePlaying() {
        if (isAudioTrackPlaying) {
            pausePlaying();
        } else {
            startPlaying();
        }
        return isAudioTrackPlaying;
    }
}
