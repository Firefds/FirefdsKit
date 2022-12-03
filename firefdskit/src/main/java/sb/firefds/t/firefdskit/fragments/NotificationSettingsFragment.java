/*
 * Copyright (C) 2022 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.t.firefdskit.fragments;

import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Optional;

import sb.firefds.t.firefdskit.R;
import sb.firefds.t.firefdskit.utils.Utils;

import static sb.firefds.t.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;
import static sb.firefds.t.firefdskit.utils.Preferences.PREF_SHOW_AM_PM;

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

        Optional<ListPreference> clockDatePreference = Optional.ofNullable(findPreference(PREF_CLOCK_DATE_PREFERENCE));
        Optional<SwitchPreferenceCompat> clockDateOnRight =
                Optional.ofNullable(findPreference(PREF_CLOCK_DATE_ON_RIGHT));
        if (clockDatePreference.isPresent() && clockDateOnRight.isPresent()) {
            clockDateOnRight.get().setEnabled(!clockDatePreference.get().getValue().equals("disabled"));
        }

        clockDatePreference.ifPresent(listPreference -> listPreference.setOnPreferenceChangeListener((preference, o) -> {
            if (o.toString().equals("disabled")) {
                clockDateOnRight.ifPresent(switchPreferenceCompat -> {
                    switchPreferenceCompat.setEnabled(false);
                    switchPreferenceCompat.setChecked(false);
                });
            } else {
                clockDateOnRight.ifPresent(switchPreferenceCompat -> switchPreferenceCompat.setEnabled(true));
            }
            return true;
        }));

        Optional<SwitchPreferenceCompat> showAmPmPreference = Optional.ofNullable(findPreference(PREF_SHOW_AM_PM));
        if (DateFormat.is24HourFormat(getFragmentActivity()) && showAmPmPreference.isPresent()) {
            showAmPmPreference.get().setEnabled(false);
            showAmPmPreference.get().setChecked(false);
        }
    }
}
