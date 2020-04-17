package sb.firefds.pie.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;

import java.util.Objects;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY_CONFIRMATION;

@Keep
public class PowerMenuSettingsFragment extends FirefdsPreferenceFragment {

    private AppCompatActivity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.advanced_power_menu_settings, rootKey);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public String getFragmentName() {
        return "powerMenuSettings";
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_CUSTOM_RECOVERY)) {
            if (Objects.equals(sharedPreferences.getString(key, ""), "")) {
                sharedPreferences.edit()
                        .putString(key, activity.getString(R.string.reboot_recovery))
                        .apply();
                EditTextPreference custom_recovery = findPreference(PREF_CUSTOM_RECOVERY);
                if (custom_recovery != null) {
                    custom_recovery.setText(activity.getString(R.string.reboot_recovery));
                }
            }
        }

        if (key.equals(PREF_CUSTOM_RECOVERY_CONFIRMATION)) {
            if (Objects.equals(sharedPreferences.getString(key, ""), "")) {
                sharedPreferences.edit()
                        .putString(key, activity.getString(R.string.reboot_confirm_recovery))
                        .apply();
                EditTextPreference custom_recovery_confirmation = findPreference(PREF_CUSTOM_RECOVERY_CONFIRMATION);
                if (custom_recovery_confirmation != null) {
                    custom_recovery_confirmation.setText(activity.getString(R.string.reboot_confirm_recovery));
                }
            }
        }
        super.onSharedPreferenceChanged(sharedPreferences, key);
    }
}
