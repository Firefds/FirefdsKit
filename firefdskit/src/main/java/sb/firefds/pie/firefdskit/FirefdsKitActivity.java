/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
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
package sb.firefds.pie.firefdskit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sb.firefds.pie.firefdskit.dialogs.CreditDialog;
import sb.firefds.pie.firefdskit.dialogs.RestoreDialog;
import sb.firefds.pie.firefdskit.dialogs.SaveDialog;
import sb.firefds.pie.firefdskit.fragments.FirefdsPreferenceFragment;
import sb.firefds.pie.firefdskit.fragments.LockscreenSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.MessagingSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.NotificationSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.PhoneSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.PowerMenuSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.ScreenTimeoutSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.SecuritySettingsFragment;
import sb.firefds.pie.firefdskit.fragments.SoundSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.SystemSettingsFragment;
import sb.firefds.pie.firefdskit.fragments.TouchwizLauncherSettingsFragment;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.PREFS;
import static sb.firefds.pie.firefdskit.utils.Constants.SHORTCUT_PHONE;
import static sb.firefds.pie.firefdskit.utils.Constants.SHORTCUT_SECURITY;
import static sb.firefds.pie.firefdskit.utils.Constants.SHORTCUT_STATUSBAR;
import static sb.firefds.pie.firefdskit.utils.Constants.SHORTCUT_SYSTEM;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_FIRST_LAUNCH;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NAVIGATION_BAR_COLOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NFC_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;

public class FirefdsKitActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RestoreDialog.RestoreDialogListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static SharedPreferences sharedPreferences;
    private static AppCompatActivity activity;
    private static Context appContext;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private ActionBarDrawerToggle menuToggle;
    private MenuItem selectedMenuItem;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appContext = Utils.isDeviceEncrypted() ? createDeviceProtectedStorageContext() : this;
        sharedPreferences = appContext.getSharedPreferences(PREFS, 0);
        activity = this;
        verifyStoragePermissions(this);

        if (Utils.isNotSamsungRom()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.samsung_rom_warning));

            alertDialogBuilder.setMessage(getString(R.string.samsung_rom_warning_msg))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_btn), null);

            alertDialogBuilder.create().show();
        }

        setContentView(R.layout.firefds_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.firefds_main);
        toggle = new ActionBarDrawerToggle(activity,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        menuToggle = toggle;
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getHeaderView(0)
                .findViewById(R.id.firefds_logo)
                .setOnClickListener(v -> showHomePage());

        setDefaultPreferences(false);

        Editor editor = sharedPreferences.edit();

        if (sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_HOURS, 0) == 0 &&
                sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_MINUTES, 0) == 0 &&
                sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_SECONDS, 0) == 0) {

            int screenTimeout = 0;
            try {
                screenTimeout = Settings.System
                        .getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            int hour = screenTimeout / 3600000;
            int min = (screenTimeout % 3600000) / 60000;
            int seconds = ((screenTimeout % 3600000) % 60000) / 1000;
            editor.putInt(PREF_SCREEN_TIMEOUT_HOURS, hour).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_MINUTES, min).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_SECONDS, seconds).apply();
        }

        if (sharedPreferences.getInt(PREF_NAVIGATION_BAR_COLOR, 0) == 0) {
            try {
                editor.putInt(PREF_NAVIGATION_BAR_COLOR,
                        Settings.Global.getInt(getContentResolver(), "navigationbar_color"))
                        .apply();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        fixAppPermissions(appContext);

        if (!XposedChecker.isActive()) {
            findViewById(R.id.root_status_container).setVisibility(View.GONE);
            findViewById(R.id.root_status_text).setVisibility(View.GONE);
            setCardStatus(R.id.xposed_status_container,
                    R.id.xposed_status_icon,
                    R.id.xposed_status_text,
                    R.drawable.ic_error,
                    R.string.firefds_kit_is_not_active,
                    R.color.error);
        } else {
            setCardStatus(R.id.xposed_status_container,
                    R.id.xposed_status_icon,
                    R.id.xposed_status_text,
                    R.drawable.ic_check_circle,
                    R.string.xposed_status,
                    R.color.active);
            new CheckRootTask().execute();
        }

        Menu menuNav = navigationView.getMenu();
        String shortcutAction = getIntent().getAction();
        if (shortcutAction != null) {
            switch (shortcutAction) {
                case SHORTCUT_STATUSBAR:
                    onNavigationItemSelected(menuNav.findItem(R.id.statusbarKey));
                    break;
                case SHORTCUT_SYSTEM:
                    onNavigationItemSelected(menuNav.findItem(R.id.systemKey));
                    break;
                case SHORTCUT_PHONE:
                    onNavigationItemSelected(menuNav.findItem(R.id.phoneKey));
                    break;
                case SHORTCUT_SECURITY:
                    onNavigationItemSelected(menuNav.findItem(R.id.securityKey));
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getVisibleFragment() instanceof ScreenTimeoutSettingsFragment ||
                getVisibleFragment() instanceof PowerMenuSettingsFragment) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.system);
                toggle = menuToggle;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                toggle.setDrawerIndicatorEnabled(true);
            }
            super.onBackPressed();
        } else {
            if (getVisibleFragment() instanceof FirefdsPreferenceFragment) {
                showHomePage();
            } else {
                new QuitTask().execute(this);
            }
        }
    }

    @Override
    public void onRestoreDefaults() {

        sharedPreferences.edit().clear().apply();
        setDefaultPreferences(true);

        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(sharedPreferences);
        }

        recreate();
        Toast.makeText(activity, R.string.defaults_restored, Toast.LENGTH_LONG).show();
        RebootNotification.notify(activity, 999, false);
    }

    @Override
    public void onRestoreBackup(final File backup) {
        new RestoreBackupTask(backup).execute();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        if (getSupportActionBar() != null) {
            toggle.setDrawerIndicatorEnabled(false);
            toggle.setToolbarNavigationClickListener(v -> onBackPressed());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(pref.getTitle());
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_credits:
                showCreditsDialog();
                break;
            case R.id.recommended_settings:
                showRecommendedSettingsDialog();
                break;
            case R.id.action_save:
                showSaveDialog();
                break;
            case R.id.action_restore:
                showRestoreDialog();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment newFragment;

        CardView cardRootView = findViewById(R.id.card_root_view);
        CardView cardXposedView = findViewById(R.id.card_xposed_view);
        cardRootView.setVisibility(View.GONE);
        cardXposedView.setVisibility(View.GONE);
        selectedMenuItem = item;

        switch (item.getItemId()) {
            case R.id.statusbarKey:
                newFragment = new NotificationSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("statusbarKey").commit();
                break;
            case R.id.lockscreenKey:
                newFragment = new LockscreenSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("lockscreenKey").commit();
                break;
            case R.id.soundKey:
                newFragment = new SoundSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("soundKey").commit();
                break;
            case R.id.systemKey:
                newFragment = new SystemSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("systemKey").commit();
                break;
            case R.id.phoneKey:
                newFragment = new PhoneSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("phoneKey").commit();
                break;
            case R.id.messagingKey:
                newFragment = new MessagingSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("messagingKey").commit();
                break;
            case R.id.securityKey:
                newFragment = new SecuritySettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("securityKey").commit();
                break;
            case R.id.launcherKey:
                newFragment = new TouchwizLauncherSettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, newFragment)
                        .addToBackStack("launcherKey").commit();
                break;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(item.getTitle());
        }
        DrawerLayout drawer = findViewById(R.id.firefds_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHomePage() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
        drawer.closeDrawer(GravityCompat.START);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        CardView cardRootView = findViewById(R.id.card_root_view);
        CardView cardXposedView = findViewById(R.id.card_xposed_view);
        cardRootView.setVisibility(View.VISIBLE);
        cardXposedView.setVisibility(View.VISIBLE);
        if (selectedMenuItem!=null) {
            selectedMenuItem.setChecked(false);
        }
    }

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    private void showCreditsDialog() {

        CreditDialog creditDialog = new CreditDialog();
        creditDialog.getDialog(activity).show();
    }

    private void showRecommendedSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true)
                .setTitle(R.string.app_name)
                .setMessage(R.string.set_recommended_settings)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.apply, (dialog, which) -> restoreRecommendedSettings())
                .create()
                .show();
    }

    private void showSaveDialog() {
        SaveDialog saveDialog = new SaveDialog();
        saveDialog.showDialog(activity, findViewById(android.R.id.content));
    }

    private void showRestoreDialog() {
        RestoreDialog restoreDialog = new RestoreDialog(this);
        restoreDialog.showDialog(activity);
    }

    private void restoreRecommendedSettings() {

        sharedPreferences.edit().clear().apply();
        setDefaultPreferences(true);

        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(sharedPreferences);
        }

        recreate();
        Toast.makeText(activity, R.string.recommended_restored, Toast.LENGTH_LONG).show();
        RebootNotification.notify(activity, 999, false);
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static Context getActivity() {
        return activity;
    }

    public static Context getAppContext() {
        return appContext;
    }

    private static void setCardStatus(int containerLayoutId,
                                      int iconLayoutId,
                                      int textLayoutId,
                                      int statusIconId,
                                      int statusTextId,
                                      int statusColorId) {

        FrameLayout statusContainerLayout = activity.findViewById(containerLayoutId);
        ImageView statusIcon = activity.findViewById(iconLayoutId);
        TextView statusText = activity.findViewById(textLayoutId);

        statusContainerLayout.setBackgroundColor(activity.getColor(statusColorId));
        statusIcon.setImageDrawable(activity.getDrawable(statusIconId));
        statusText.setText(statusTextId);
        statusText.setTextColor(Color.WHITE);
    }

    private static void verifyStoragePermissions(AppCompatActivity activity) {
        // Check if we have write settings permission
        if (!Settings.System.canWrite(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    public static void fixPermissions(Context context) {
        File sharedPrefsFolder =
                new File(context.getDataDir().getAbsolutePath() + "/shared_prefs");
        if (sharedPrefsFolder.exists()) {
            sharedPrefsFolder.setExecutable(true, false);
            sharedPrefsFolder.setReadable(true, false);
            File f = new File(String.format("%s/%s_preferences.xml",
                    sharedPrefsFolder.getAbsolutePath(),
                    BuildConfig.APPLICATION_ID));
            if (f.exists()) {
                f.setReadable(true, false);
                f.setExecutable(true, false);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    private static void fixAppPermissions(Context context) {
        File appFolder = context.getFilesDir().getParentFile();
        appFolder.setExecutable(true, false);
        appFolder.setReadable(true, false);
    }

    private static void setDefaultPreferences(boolean forceDefault) {
        upgradePreferences();
        PreferenceManager.setDefaultValues(appContext, R.xml.lockscreen_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.messaging_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.notification_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.phone_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.security_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.sound_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.system_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.advanced_power_menu_settings, true);
        PreferenceManager.setDefaultValues(appContext, R.xml.touchwiz_launcher_settings, true);
        if (forceDefault) {
            Editor editor = sharedPreferences.edit();

            editor.putInt(PREF_SCREEN_TIMEOUT_SECONDS, 30).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_MINUTES, 0).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_HOURS, 0).apply();

            editor.putInt(PREF_NAVIGATION_BAR_COLOR,
                    activity.getResources()
                            .getIntArray(R.array.navigationbar_color_values)[1]).apply();
        }
        fixPermissions(appContext);
    }

    private static void upgradePreferences() {
        SharedPreferences preferences = getSharedPreferences();
        try {
            preferences.getString(PREF_DATA_ICON_BEHAVIOR, "0");
        } catch (ClassCastException e) {
            String uid = String.valueOf(preferences.getInt(PREF_DATA_ICON_BEHAVIOR, 0));
            preferences.edit().putString(PREF_DATA_ICON_BEHAVIOR, uid).apply();
        }
        try {
            preferences.getString(PREF_NFC_BEHAVIOR, "0");
        } catch (ClassCastException e) {
            String uid = String.valueOf(preferences.getInt(PREF_NFC_BEHAVIOR, 0));
            preferences.edit().putString(PREF_NFC_BEHAVIOR, uid).apply();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class RestoreBackupTask extends AsyncTask<Void, Void, Void> {

        private File backup;

        RestoreBackupTask(File backup) {
            this.backup = backup;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(backup));
                Editor prefEdit = sharedPreferences.edit();
                prefEdit.clear();
                @SuppressWarnings("unchecked")
                Map<String, ?> entries = (Map<String, ?>) input.readObject();
                for (Entry<String, ?> entry : entries.entrySet()) {
                    Object v = entry.getValue();
                    String key = entry.getKey();

                    if (v instanceof Boolean)
                        prefEdit.putBoolean(key, (Boolean) v);
                    else if (v instanceof Float)
                        prefEdit.putFloat(key, (Float) v);
                    else if (v instanceof Integer)
                        prefEdit.putInt(key, (Integer) v);
                    else if (v instanceof Long)
                        prefEdit.putLong(key, (Long) v);
                    else if (v instanceof String)
                        prefEdit.putString(key, ((String) v));
                }
                prefEdit.apply();
                fixPermissions(appContext);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            SystemClock.sleep(1500);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Utils.createSnackbar(findViewById(android.R.id.content),
                    R.string.backup_restored,
                    activity).show();
            RebootNotification.notify(activity, 999, false);
        }
    }

    private static class QuitTask extends AsyncTask<AppCompatActivity, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        private AppCompatActivity mActivity = null;

        @Override
        protected Void doInBackground(AppCompatActivity... appCompatActivities) {
            try {
                mActivity = appCompatActivities[0];
                if (!Utils.isOmcEncryptedFlag()) {
                    XCscFeaturesManager.applyCscFeatures(sharedPreferences);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                Utils.resetPermissions(mActivity);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (mActivity != null) {
                    mActivity.finish();
                }
            }
            super.onPostExecute(result);
        }

    }

    private static class CheckRootTask extends AsyncTask<Void, Void, Void> {
        private boolean suAvailable = false;

        protected Void doInBackground(Void... params) {
            try {
                suAvailable = Shell.getShell().isRoot();

            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void p) {

            try {
                activity.findViewById(R.id.root_progressBar).setVisibility(View.GONE);
                activity.findViewById(R.id.root_status_icon).setVisibility(View.VISIBLE);

                // Check for root access
                if (!suAvailable) {
                    setCardStatus(R.id.root_status_container,
                            R.id.root_status_icon,
                            R.id.root_status_text,
                            R.drawable.ic_warning,
                            R.string.root_info,
                            R.color.warning);
                } else {
                    new CopyCSCTask().execute(activity);
                    if (!sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, false)) {
                        new AlertDialog.Builder(activity)
                                .setCancelable(true)
                                .setIcon(R.drawable.ic_warning)
                                .setTitle(R.string.app_name)
                                .setMessage(R.string.firefds_xposed_disclaimer)
                                .setPositiveButton(android.R.string.ok,
                                        (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                        sharedPreferences.edit().putBoolean(PREF_FIRST_LAUNCH, true).apply();
                    }
                    setCardStatus(R.id.root_status_container,
                            R.id.root_status_icon,
                            R.id.root_status_text,
                            R.drawable.ic_check_circle,
                            R.string.root_granted,
                            R.color.active);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static class CopyCSCTask extends AsyncTask<Context, Void, Void> {

        protected Void doInBackground(Context... params) {
            try {
                if (!Utils.isOmcEncryptedFlag()) {
                    XCscFeaturesManager.getDefaultCSCFeaturesFromFiles();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (!Utils.isOmcEncryptedFlag()) {
                    Utils.createCSCFiles(activity);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }
}
