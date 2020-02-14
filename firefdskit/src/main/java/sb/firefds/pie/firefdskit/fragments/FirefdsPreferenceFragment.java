package sb.firefds.pie.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.List;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.FirefdsKitActivity.fixPermissions;
import static sb.firefds.pie.firefdskit.FirefdsKitActivity.getAppContext;
import static sb.firefds.pie.firefdskit.FirefdsKitActivity.getSharedPreferences;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;

public class FirefdsPreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static AppCompatActivity fragmentActivity;
    private static List<String> changesMade;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = (AppCompatActivity) getActivity();
        if (changesMade == null) {
            changesMade = new ArrayList<>();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        try {
            // No reboot notification required
            String[] litePrefs = fragmentActivity.getResources().getStringArray(R.array.lite_preferences);

            setTimeoutPrefs(sharedPreferences, key);

            for (String string : litePrefs) {
                if (key.equalsIgnoreCase(string)) {
                    fixPermissions(getAppContext());
                    return;
                }
            }

            // Add preference key to changed keys list
            if (!changesMade.contains(key)) {
                changesMade.add(key);
            }
            fixPermissions(getAppContext());
            RebootNotification.notify(fragmentActivity, changesMade.size(), true);
        } catch (Throwable e) {
            Utils.log(e);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onResume() {
        super.onResume();
        registerPrefsReceiver();
        fixPermissions(getAppContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterPrefsReceiver();
        fixPermissions(getAppContext());
    }

    private void setTimeoutPrefs(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_SCREEN_TIMEOUT_SECONDS) ||
                key.equals(PREF_SCREEN_TIMEOUT_MINUTES) ||
                key.equals(PREF_SCREEN_TIMEOUT_HOURS)) {

            int hour = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_HOURS, 0) * 3600000;
            int min = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_MINUTES, 0) * 60000;
            int sec = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_SECONDS, 30) * 1000;
            int timeoutML = hour + min + sec;
            Settings.System.putInt(fragmentActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeoutML);
        }
    }

    private void registerPrefsReceiver() {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPrefsReceiver() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
