package sb.firefds.pie.firefdskit.fragments;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import sb.firefds.pie.firefdskit.R;

public class PreferenceFragmentFactory {

    private final static Map<Integer, Supplier<FirefdsPreferenceFragment>>
            MENU_FRAGMENT_SUPPLIER_MAP = new HashMap<>();

    private final static Supplier<FirefdsPreferenceFragment> NOTIFICATION_SETTINGS_FRAGMENT = NotificationSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> LOCKSCREEN_SETTINGS_FRAGMENT = LockscreenSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> SOUND_SETTINGS_FRAGMENT = SoundSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> SYSTEM_SETTINGS_FRAGMENT = SystemSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> PHONE_SETTINGS_FRAGMENT = PhoneSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> MESSAGING_SETTINGS_FRAGMENT = MessagingSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> SECURITY_SETTINGS_FRAGMENT = SecuritySettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> TOUCHWIZ_LAUNCHER_SETTINGS_FRAGMENT = TouchwizLauncherSettingsFragment::new;
    private final static Supplier<FirefdsPreferenceFragment> FIREFDS_KIT_SETTINGS_FRAGMENT = FirefdsKitSettingsFragment::new;

    static {
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.statusbarKey, NOTIFICATION_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.lockscreenKey, LOCKSCREEN_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.soundKey, SOUND_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.systemKey, SYSTEM_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.phoneKey, PHONE_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.messagingKey, MESSAGING_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.securityKey, SECURITY_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.launcherKey, TOUCHWIZ_LAUNCHER_SETTINGS_FRAGMENT);
        MENU_FRAGMENT_SUPPLIER_MAP.put(R.id.firefdsKitKey, FIREFDS_KIT_SETTINGS_FRAGMENT);
    }

    public static FirefdsPreferenceFragment getMenuFragment(int menuId) {
        return Objects.requireNonNull(MENU_FRAGMENT_SUPPLIER_MAP.get(menuId)).get();
    }
}