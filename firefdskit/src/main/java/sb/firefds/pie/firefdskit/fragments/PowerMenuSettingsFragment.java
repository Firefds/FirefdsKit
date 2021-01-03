/*
 * Copyright (C) 2020 Shauli Bracha for FirefdsKit Project (firefds@xda)
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
package sb.firefds.pie.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.EditTextPreference;

import java.util.Optional;

import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY_CONFIRMATION;

@Keep
public class PowerMenuSettingsFragment extends FirefdsPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        setPreferencesFromResource(R.xml.advanced_power_menu_settings, rootKey);
    }

    @Override
    public String getFragmentName() {
        return "powerMenuSettings";
    }

    @Override
    public boolean isSubFragment() {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_CUSTOM_RECOVERY)) {
            if (sharedPreferences.getString(key, "").equals("")) {
                sharedPreferences.edit()
                        .putString(key, getFragmentActivity().getString(R.string.reboot_recovery))
                        .apply();
                Optional<EditTextPreference> custom_recovery =
                        Optional.ofNullable(findPreference(PREF_CUSTOM_RECOVERY));
                custom_recovery.ifPresent(editTextPreference ->
                        editTextPreference.setText(getFragmentActivity().getString(R.string.reboot_recovery)));
            }
        }

        if (key.equals(PREF_CUSTOM_RECOVERY_CONFIRMATION)) {
            if (sharedPreferences.getString(key, "").equals("")) {
                sharedPreferences.edit()
                        .putString(key, getFragmentActivity().getString(R.string.reboot_confirm_recovery))
                        .apply();
                Optional<EditTextPreference> custom_recovery_confirmation =
                        Optional.ofNullable(findPreference(PREF_CUSTOM_RECOVERY_CONFIRMATION));
                custom_recovery_confirmation.ifPresent(editTextPreference ->
                        editTextPreference.setText(getFragmentActivity().getString(R.string.reboot_confirm_recovery)));
            }
        }
        super.onSharedPreferenceChanged(sharedPreferences, key);
    }
}
