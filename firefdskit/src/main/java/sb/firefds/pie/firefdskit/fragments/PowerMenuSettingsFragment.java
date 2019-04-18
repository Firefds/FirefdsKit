package sb.firefds.pie.firefdskit.fragments;

import android.os.Bundle;

import androidx.annotation.Keep;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

@Keep
public class PowerMenuSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.advanced_power_menu_settings, rootKey);
    }
}
