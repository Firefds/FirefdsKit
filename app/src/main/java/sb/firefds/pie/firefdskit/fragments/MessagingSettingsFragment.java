package sb.firefds.pie.firefdskit.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;
import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_SMS_TO_MMS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_FORCE_MMS_CONNECT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_MESSAGING_KEY_CAT;

public class MessagingSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.messaging_settings, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isOmcEncryptedFlag()) {
            PreferenceCategory messagingScreen = findPreference(PREF_MESSAGING_KEY_CAT);
            SwitchPreferenceCompat disableSmsToMmsConversionPreference =
                    findPreference(PREF_DISABLE_SMS_TO_MMS);
            SwitchPreferenceCompat forceMMSConnectPreference =
                    findPreference(PREF_FORCE_MMS_CONNECT);

            if (messagingScreen != null) {
                if (disableSmsToMmsConversionPreference != null) {
                    messagingScreen.removePreference(disableSmsToMmsConversionPreference);
                }
                if (forceMMSConnectPreference != null) {
                    messagingScreen.removePreference(forceMMSConnectPreference);
                }
            }
        }
    }
}
