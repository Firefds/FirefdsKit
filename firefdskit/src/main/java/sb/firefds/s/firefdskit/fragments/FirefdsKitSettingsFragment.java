/*
 * Copyright (C) 2021 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.s.firefdskit.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import sb.firefds.s.firefdskit.R;
import sb.firefds.s.firefdskit.utils.Utils;

import static sb.firefds.s.firefdskit.utils.Preferences.PREF_FORCE_ENGLISH;

public class FirefdsKitSettingsFragment extends FirefdsPreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (Utils.isDeviceEncrypted()) {
            getPreferenceManager().setStorageDeviceProtected();
        }
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
