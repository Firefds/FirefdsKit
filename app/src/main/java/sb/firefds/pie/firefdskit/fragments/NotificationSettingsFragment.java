package sb.firefds.pie.firefdskit.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;
import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;

public class NotificationSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListPreference clock_date_preference = findPreference(PREF_CLOCK_DATE_PREFERENCE);
        SwitchPreferenceCompat clockDateOnRight = findPreference(PREF_CLOCK_DATE_ON_RIGHT);
        if (clockDateOnRight != null) {
            if (clock_date_preference != null) {
                clockDateOnRight.setEnabled(!clock_date_preference.getValue().equals("disabled"));
            }
        }
        if (clock_date_preference != null) {
            clock_date_preference.setOnPreferenceChangeListener((preference, o) -> {
                if (!o.toString().equals("disabled")) {
                    if (clockDateOnRight != null) {
                        clockDateOnRight.setEnabled(true);
                    }
                } else {
                    if (clockDateOnRight != null) {
                        clockDateOnRight.setEnabled(false);
                    }
                }
                return true;
            });
        }
    }
}
