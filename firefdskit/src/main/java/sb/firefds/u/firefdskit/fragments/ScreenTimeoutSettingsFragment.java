/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit.fragments;

import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.u.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import sb.firefds.u.firefdskit.R;
import sb.firefds.u.firefdskit.utils.Utils;

@Keep
public class ScreenTimeoutSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
        setPreferencesFromResource(R.xml.screen_timeout_settings, rootKey);
    }

    @Override
    public String getFragmentName() {
        return "screenTimeoutSettings";
    }

    @Override
    public boolean isSubFragment() {
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String key) {

        int hour = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_HOURS, 0) * 3600000;
        int min = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_MINUTES, 0) * 60000;
        int sec = sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_SECONDS, 30) * 1000;
        int timeoutML = hour + min + sec;
        Settings.System.putInt(getFragmentActivity().getContentResolver(),
                               Settings.System.SCREEN_OFF_TIMEOUT,
                               timeoutML);

        super.onSharedPreferenceChanged(sharedPreferences, key);
    }
}
