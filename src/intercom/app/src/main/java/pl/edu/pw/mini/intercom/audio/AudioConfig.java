package pl.edu.pw.mini.intercom.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

public class AudioConfig {
    public AudioManager audioManager;
    public AudioRecord audioRecord;
    public AudioTrack audioTrack;
    public AudioFormat audioFormat;
    public int minAudioRecordBufferInBytes;
}
