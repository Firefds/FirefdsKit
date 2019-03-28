package sb.firefds.pie.firefdskit.fragments;

import android.os.Bundle;

import sb.firefds.pie.firefdskit.R;

public class LockscreenSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.lockscreen_settings, rootKey);
    }
}
