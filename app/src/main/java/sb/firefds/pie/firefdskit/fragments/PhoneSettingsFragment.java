package sb.firefds.pie.firefdskit.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;
import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_PHONE_KEY_CAT;

public class PhoneSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.phone_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isOmcEncryptedFlag()) {
            PreferenceCategory phoneScreen = findPreference(PREF_PHONE_KEY_CAT);
            SwitchPreferenceCompat disableNumberFormattingPreference =
                    findPreference(PREF_DISABLE_NUMBER_FORMATTING);

            if (phoneScreen != null) {
                if (disableNumberFormattingPreference != null) {
                    phoneScreen.removePreference(disableNumberFormattingPreference);
                }
            }
        }
    }
}
