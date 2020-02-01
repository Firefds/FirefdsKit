package sb.firefds.q.firefdskit.fragments;

import android.os.Bundle;

import androidx.annotation.Keep;

import sb.firefds.q.firefdskit.R;
import sb.firefds.q.firefdskit.utils.Utils;

@Keep
public class ScreenTimeoutSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.screen_timeout_settings, rootKey);
    }
}
