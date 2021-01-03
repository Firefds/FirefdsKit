package sb.firefds.pie.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_FORCE_ENGLISH;

public class FirefdsKitSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        setPreferencesFromResource(R.xml.firefds_kit_settings, rootKey);
    }

    @Override
    public String getFragmentName() {
        return "firefdsKitKey";
    }

    @Override
    public boolean isSubFragment() {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_FORCE_ENGLISH))
            Toast.makeText(getActivity(), getResources().getText(R.string.language_toast), Toast.LENGTH_SHORT).show();
        super.onSharedPreferenceChanged(sharedPreferences, key);
    }
}
