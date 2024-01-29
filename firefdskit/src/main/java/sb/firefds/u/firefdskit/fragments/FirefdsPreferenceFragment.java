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

import static sb.firefds.u.firefdskit.FirefdsKitActivity.getSharedPreferences;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sb.firefds.u.firefdskit.R;
import sb.firefds.u.firefdskit.notifications.RebootNotification;
import sb.firefds.u.firefdskit.utils.Utils;

public abstract class FirefdsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppCompatActivity fragmentActivity;
    private static List<String> changesMade;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentActivity = (AppCompatActivity) getActivity();
        if (changesMade == null) {
            changesMade = new ArrayList<>();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        try {
            // No reboot notification required
            String[] rebootRequiredPrefs = fragmentActivity.getResources()
                                                           .getStringArray(R.array.reboot_required_preferences);

            boolean doesPrefRequireReboot = Arrays.asList(rebootRequiredPrefs).contains(key);
            if (!doesPrefRequireReboot) {
                return;
            }

            // Add preference key to changed keys list
            if (!changesMade.contains(key)) {
                changesMade.add(key);
            }
            RebootNotification.notify(fragmentActivity, changesMade.size(), false);
        } catch (Throwable e) {
            Utils.log(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerPrefsReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterPrefsReceiver();
    }

    public abstract String getFragmentName();

    public abstract boolean isSubFragment();

    AppCompatActivity getFragmentActivity() {
        return fragmentActivity;
    }

    private void registerPrefsReceiver() {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void unregisterPrefsReceiver() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
