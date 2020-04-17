/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sb.firefds.q.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;

import java.util.Objects;

import sb.firefds.q.firefdskit.R;
import sb.firefds.q.firefdskit.utils.Utils;

import static sb.firefds.q.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY_CONFIRMATION;

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
