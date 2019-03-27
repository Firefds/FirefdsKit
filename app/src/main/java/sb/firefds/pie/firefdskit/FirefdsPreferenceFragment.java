package sb.firefds.pie.firefdskit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.preference.PreferenceFragmentCompat;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;

import static sb.firefds.pie.firefdskit.FirefdsKitActivity.fixPermissions;
import static sb.firefds.pie.firefdskit.FirefdsKitActivity.getSharedPreferences;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;

public class FirefdsPreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("StaticFieldLeak")
    private static Context fragmentContext;
    private List<String> changesMade;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        changesMade = new ArrayList<>();
        fragmentContext = Objects.requireNonNull(getActivity()).getBaseContext();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        try {
            // No reboot notification required
            String[] litePrefs =
                    fragmentContext.getResources().getStringArray(R.array.lite_preferences);

            setTimeoutPrefs(sharedPreferences, key);

            for (String string : litePrefs) {
                if (key.equalsIgnoreCase(string)) {
                    fixPermissions(fragmentContext);
                    return;
                }
            }

            // Add preference key to changed keys list
            if (!changesMade.contains(key)) {
                changesMade.add(key);
            }
            fixPermissions(fragmentContext);
            RebootNotification.notify(fragmentContext, changesMade.size(), false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onResume() {
        super.onResume();
        registerPrefsReceiver();
        fixPermissions(fragmentContext);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterPrefsReceiver();
        fixPermissions(fragmentContext);
    }

    private void setTimeoutPrefs(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_SCREEN_TIMEOUT_SECONDS) ||
                key.equals(PREF_SCREEN_TIMEOUT_MINUTES) ||
                key.equals(PREF_SCREEN_TIMEOUT_HOURS)) {

            int hour = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_HOURS, 0) * 3600000;
            int min = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_MINUTES, 0) * 60000;
            int sec = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_SECONDS, 30) * 1000;
            int timeoutML = hour + min + sec;
            Settings.System.putInt(fragmentContext.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, timeoutML);
        }
    }

    private void registerPrefsReceiver() {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPrefsReceiver() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
