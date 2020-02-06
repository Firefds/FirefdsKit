package sb.firefds.q.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import sb.firefds.q.firefdskit.R;
import sb.firefds.q.firefdskit.utils.Utils;

public class SystemSettingsFragment extends FirefdsPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.system_settings, rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
    }
}
