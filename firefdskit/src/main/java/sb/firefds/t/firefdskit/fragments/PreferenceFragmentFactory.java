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

import androidx.core.util.Supplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import sb.firefds.t.firefdskit.R;

public class PreferenceFragmentFactory {

    private final static Map<Integer, Supplier<FirefdsPreferenceFragment>>
            MENU_FRAGMENT_SUPPLIER_MAP = new HashMap<>();

    private final static Supplier<FirefdsPreferenceFragment> NOTIFICATION_SETTINGS_FRAGMENT =
            NotificationSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> LOCKSCREEN_SETTINGS_FRAGMENT =
            LockscreenSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> SOUND_SETTINGS_FRAGMENT = SoundSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> SYSTEM_SETTINGS_FRAGMENT = SystemSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> PHONE_SETTINGS_FRAGMENT = PhoneSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> MESSAGING_SETTINGS_FRAGMENT =
            MessagingSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> SECURITY_SETTINGS_FRAGMENT = SecuritySettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> FIREFDS_KIT_SETTINGS_FRAGMENT =
            FirefdsKitSettingsFragment::new;

    static {
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.statusbarKey, NOTIFICATION_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.lockscreenKey, LOCKSCREEN_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.soundKey, SOUND_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.systemKey, SYSTEM_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.phoneKey, PHONE_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.messagingKey, MESSAGING_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.securityKey, SECURITY_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.firefdsKitKey, FIREFDS_KIT_SETTINGS_FRAGMENT);
    }

    public static Optional<FirefdsPreferenceFragment> getMenuFragment(int menuId) {
        return Optional.ofNullable(MENU_FRAGMENT_SUPPLIER_MAP.get(menuId))
                .map(Supplier::get);
    }
}
