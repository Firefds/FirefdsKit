package sb.firefds.pie.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NAVIGATION_BAR_COLOR;

public class SystemSettingsFragment extends FirefdsPreferenceFragment {

    private AppCompatActivity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        setPreferencesFromResource(R.xml.system_settings, rootKey);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key.equals(PREF_NAVIGATION_BAR_COLOR)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            TextView tv = new TextView(activity);
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setText(R.string.navigation_bar_color_dialog_message);
            tv.setPadding(16, 16, 16, 16);
            builder.setTitle(R.string.navigation_bar_color_dialog_title)
                    .setView(tv)
                    .setNeutralButton("OK", (dialog, id) -> dialog.dismiss())
                    .show();
        }
    }
}
