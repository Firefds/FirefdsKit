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

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;

public class NotificationSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.notification_settings, rootKey);
    }

    @Override
    public String getFragmentName() {
        return "statusbarKey";
    }

    @Override
    public boolean isSubFragment() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListPreference clock_date_preference = findPreference(PREF_CLOCK_DATE_PREFERENCE);
        SwitchPreferenceCompat clockDateOnRight = findPreference(PREF_CLOCK_DATE_ON_RIGHT);
        if (clockDateOnRight != null) {
            if (clock_date_preference != null) {
                clockDateOnRight.setEnabled(!clock_date_preference.getValue().equals("disabled"));
            }
        }
        if (clock_date_preference != null) {
            clock_date_preference.setOnPreferenceChangeListener((preference, o) -> {
                if (!o.toString().equals("disabled")) {
                    if (clockDateOnRight != null) {
                        clockDateOnRight.setEnabled(true);
                    }
                } else {
                    if (clockDateOnRight != null) {
                        clockDateOnRight.setEnabled(false);
                    }
                }
                return true;
            });
        }
    }
}
