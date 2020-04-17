package sb.firefds.pie.firefdskit.fragments;

import android.os.Bundle;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

public class PhoneSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.phone_settings, rootKey);
    }

    @Override
    public String getFragmentName() {
        return "phoneKey";
    }
}
