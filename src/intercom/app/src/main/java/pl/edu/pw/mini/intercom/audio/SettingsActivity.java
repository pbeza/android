package pl.edu.pw.mini.intercom.audio;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import pl.edu.pw.mini.intercom.config.EchoConfigApplication;
import pl.edu.pw.mini.intercom.gui.SettingsFragment;

public class SettingsActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "SettingsActivity";

    public static final String FRAGMENT_TAG = "settingsFragment";
    public static final String KEY_PREF_SAMPLE_RATE = "pref_sampleRate";

    private SettingsFragment settingsFragment;


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


//        if (key.equals(KEY_PREF_SAMPLE_RATE)) {
//            Preference pref = settingsFragment.findPreference(key);
//            pref.setSummary(sharedPreferences.getString(key, ""));
//        }
    }


    @Override
    protected void onResume() {
        super.onResume();


        settingsFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        settingsFragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment, FRAGMENT_TAG)
                .commit();

    }

    @Override
    protected void onStop() {
        super.onStop();


    }
}


