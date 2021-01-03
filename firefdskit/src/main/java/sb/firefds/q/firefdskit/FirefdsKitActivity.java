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
package sb.firefds.q.firefdskit;

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
import android.util.Log;
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
import androidx.arch.core.util.Function;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.samsung.android.feature.SemCscFeature;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import sb.firefds.q.firefdskit.dialogs.CreditDialog;
import sb.firefds.q.firefdskit.dialogs.RestoreDialog;
import sb.firefds.q.firefdskit.dialogs.SaveDialog;
import sb.firefds.q.firefdskit.fragments.FirefdsPreferenceFragment;
import sb.firefds.q.firefdskit.fragments.PreferenceFragmentFactory;
import sb.firefds.q.firefdskit.notifications.RebootNotification;
import sb.firefds.q.firefdskit.utils.Utils;

import static sb.firefds.q.firefdskit.utils.Constants.DISABLE_PHONE_NUMBER_FORMATTING;
import static sb.firefds.q.firefdskit.utils.Constants.DISABLE_SMS_TO_MMS_CONVERSION_BY_TEXT_INPUT;
import static sb.firefds.q.firefdskit.utils.Constants.FORCE_CONNECT_MMS;
import static sb.firefds.q.firefdskit.utils.Constants.PREFS;
import static sb.firefds.q.firefdskit.utils.Constants.SHORTCUT_PHONE;
import static sb.firefds.q.firefdskit.utils.Constants.SHORTCUT_SECURITY;
import static sb.firefds.q.firefdskit.utils.Constants.SHORTCUT_STATUSBAR;
import static sb.firefds.q.firefdskit.utils.Constants.SHORTCUT_SYSTEM;
import static sb.firefds.q.firefdskit.utils.Packages.FIREFDSKIT;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_DISABLE_SMS_TO_MMS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_FIRST_LAUNCH;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_FORCE_MMS_CONNECT;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_NFC_BEHAVIOR;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;
import static sb.firefds.q.firefdskit.utils.Utils.checkForceEnglish;
import static sb.firefds.q.firefdskit.utils.Utils.isNotSamsungRom;
import static sb.firefds.q.firefdskit.utils.Utils.log;

public class FirefdsKitActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RestoreDialog.RestoreDialogListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private final Runnable SHOW_CREDIT_DIALOG = this::showCreditsDialog;
    private final Runnable SHOW_RECOMMENDED_SETTINGS_DIALOG = this::showRecommendedSettingsDialog;
    private final Runnable SHOW_SAVE_DIALOG = this::showSaveDialog;
    private final Runnable SHOW_RESTORE_DIALOG = this::showRestoreDialog;
    private final Runnable ON_BACK_PRESSED = this::onBackPressed;
    private final Function<Menu, MenuItem> SHORTCUT_STATUSBAR_ITEM = (m) -> m.findItem(R.id.statusbarKey);
    private final Function<Menu, MenuItem> SHORTCUT_SYSTEM_ITEM = (m) -> m.findItem(R.id.systemKey);
    private final Function<Menu, MenuItem> SHORTCUT_PHONE_ITEM = (m) -> m.findItem(R.id.phoneKey);
    private final Function<Menu, MenuItem> SHORTCUT_SECURITY_ITEM = (m) -> m.findItem(R.id.securityKey);
    private final Map<Integer, Runnable> OPTIONS_ITEMS = new HashMap<>();
    private final Map<String, Function<Menu, MenuItem>> SHORTCUTS_ITEMS = new HashMap<>();
    private static SharedPreferences sharedPreferences;
    private static AppCompatActivity activity;
    private static Context appContext;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private ActionBarDrawerToggle menuToggle;
    private MenuItem selectedMenuItem;
    private static String mPreferenceDir;

    {
        OPTIONS_ITEMS.put(R.id.action_credits, SHOW_CREDIT_DIALOG);
        OPTIONS_ITEMS.put(R.id.recommended_settings, SHOW_RECOMMENDED_SETTINGS_DIALOG);
        OPTIONS_ITEMS.put(R.id.action_save, SHOW_SAVE_DIALOG);
        OPTIONS_ITEMS.put(R.id.action_restore, SHOW_RESTORE_DIALOG);
        OPTIONS_ITEMS.put(R.id.home, ON_BACK_PRESSED);
        SHORTCUTS_ITEMS.put(SHORTCUT_STATUSBAR, SHORTCUT_STATUSBAR_ITEM);
        SHORTCUTS_ITEMS.put(SHORTCUT_SYSTEM, SHORTCUT_SYSTEM_ITEM);
        SHORTCUTS_ITEMS.put(SHORTCUT_PHONE, SHORTCUT_PHONE_ITEM);
        SHORTCUTS_ITEMS.put(SHORTCUT_SECURITY, SHORTCUT_SECURITY_ITEM);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appContext = !this.isDeviceProtectedStorage() ? createDeviceProtectedStorageContext() : this;
        sharedPreferences = appContext.getSharedPreferences(PREFS, 0);
        activity = this;
        verifyStoragePermissions(this);

        if (isNotSamsungRom()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.samsung_rom_warning));

            alertDialogBuilder.setMessage(getString(R.string.samsung_rom_warning_msg))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_btn), null);

            alertDialogBuilder.create().show();
        }

        setContentView(R.layout.firefds_main);

        if (savedInstanceState != null) {
            CardView cardXposedView = findViewById(R.id.card_xposed_view);
            cardXposedView.setVisibility(View.GONE);
        }

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
                screenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            } catch (Throwable e) {
                log(e);
            }
            int hour = screenTimeout / 3600000;
            int min = (screenTimeout % 3600000) / 60000;
            int seconds = ((screenTimeout % 3600000) % 60000) / 1000;
            editor.putInt(PREF_SCREEN_TIMEOUT_HOURS, hour).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_MINUTES, min).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_SECONDS, seconds).apply();
        }

        fixAppPermissions(appContext);

        if (!XposedChecker.isActive()) {
            setCardStatus(R.drawable.ic_error,
                    R.string.firefds_kit_is_not_active,
                    R.color.error);
        } else {
            setCardStatus(R.drawable.ic_check_circle,
                    R.string.xposed_status,
                    R.color.active);

            if (!sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, false)) {
                new AlertDialog.Builder(activity)
                        .setCancelable(true)
                        .setIcon(R.drawable.ic_warning)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.firefds_xposed_disclaimer)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
                sharedPreferences.edit().putBoolean(PREF_FIRST_LAUNCH, true).apply();
            }
        }

        Menu menuNav = navigationView.getMenu();
        Optional.of(getIntent())
                .map(Intent::getAction)
                .ifPresent(action -> openMenuItem(action, menuNav));
    }

    @Override
    public void onBackPressed() {
        getVisibleFragment().ifPresent(fragment -> {
            if (((FirefdsPreferenceFragment) fragment).isSubFragment()) {
                Optional.of(this)
                        .map(AppCompatActivity::getSupportActionBar)
                        .ifPresent(actionBar -> {
                            actionBar.setTitle(R.string.system);
                            toggle = menuToggle;
                            actionBar.setDisplayHomeAsUpEnabled(false);
                            toggle.setDrawerIndicatorEnabled(true);
                        });

                super.onBackPressed();
            } else {
                showHomePage();
            }
        });

        if (!getVisibleFragment().isPresent()) {
            this.finish();
        }
    }

    @Override
    public void onRestoreDefaults() {

        sharedPreferences.edit().clear().apply();
        setDefaultPreferences(true);

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
        Optional.of(this)
                .map(AppCompatActivity::getSupportActionBar)
                .ifPresent(actionBar -> {
                    toggle.setDrawerIndicatorEnabled(false);
                    toggle.setToolbarNavigationClickListener(v -> onBackPressed());
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setTitle(pref.getTitle());
                });
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
        runMenuItemOption(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        CardView cardXposedView = findViewById(R.id.card_xposed_view);
        cardXposedView.setVisibility(View.GONE);
        selectedMenuItem = item;

        PreferenceFragmentFactory.getMenuFragment(item.getItemId())
                .ifPresent(firefdsPreferenceFragment -> getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_main, firefdsPreferenceFragment)
                        .addToBackStack(firefdsPreferenceFragment.getFragmentName()).commit());
        Optional.of(this)
                .map(AppCompatActivity::getSupportActionBar)
                .ifPresent(actionBar -> actionBar.setTitle(item.getTitle()));
        DrawerLayout drawer = findViewById(R.id.firefds_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Context tempContext = !newBase.isDeviceProtectedStorage() ? newBase.createDeviceProtectedStorageContext() : newBase;
        Context context = checkForceEnglish(newBase, tempContext.getSharedPreferences(PREFS, MODE_PRIVATE));
        super.attachBaseContext(context);
    }

    private void runMenuItemOption(int menuItemId) {
        Optional.of(OPTIONS_ITEMS)
                .map(functionMap -> functionMap.get(menuItemId))
                .ifPresent(Runnable::run);
    }

    private void openMenuItem(String shortcutAction, Menu menuNav) {
        Optional.of(SHORTCUTS_ITEMS)
                .map(functionMap -> functionMap.get(shortcutAction))
                .map(itemFunction -> itemFunction.apply(menuNav))
                .ifPresent(this::onNavigationItemSelected);
    }

    private void showHomePage() {
        Optional.of(this)
                .map(AppCompatActivity::getSupportActionBar)
                .ifPresent(actionBar -> actionBar.setTitle(R.string.app_name));
        drawer.closeDrawer(GravityCompat.START);
        getSupportFragmentManager().getFragments().forEach(fragment ->
                getSupportFragmentManager().beginTransaction().remove(fragment).commit());
        CardView cardXposedView = findViewById(R.id.card_xposed_view);
        cardXposedView.setVisibility(View.VISIBLE);
        Optional.ofNullable(selectedMenuItem)
                .ifPresent(menuItem -> menuItem.setChecked(false));
    }

    private Optional<Fragment> getVisibleFragment() {
        return getSupportFragmentManager().getFragments().stream()
                .filter(Fragment::isVisible)
                .findFirst();
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

        recreate();
        Toast.makeText(activity, R.string.recommended_restored, Toast.LENGTH_LONG).show();
        RebootNotification.notify(activity, 999, false);
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static Context getAppContext() {
        return appContext;
    }

    private static void setCardStatus(int statusIconId,
                                      int statusTextId,
                                      int statusColorId) {

        FrameLayout statusContainerLayout = activity.findViewById(R.id.xposed_status_container);
        ImageView statusIcon = activity.findViewById(R.id.xposed_status_icon);
        TextView statusText = activity.findViewById(R.id.xposed_status_text);

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
    public static void fixPermissions() {
        File sharedPrefsFolder = new File(getPreferenceDir());
        if (sharedPrefsFolder.exists()) {
            sharedPrefsFolder.setExecutable(true, false);
            sharedPrefsFolder.setReadable(true, false);
            File f = new File(String.format("%s/%s_preferences.xml",
                    sharedPrefsFolder.getAbsolutePath(),
                    FIREFDSKIT));
            if (f.exists()) {
                f.setReadable(true, false);
                f.setExecutable(true, false);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    private static void fixAppPermissions(Context context) {
        Optional.of(context)
                .map(Context::getFilesDir)
                .map(File::getParentFile)
                .ifPresent(folder -> {
                    folder.setExecutable(true, false);
                    folder.setReadable(true, false);
                });
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
        PreferenceManager.setDefaultValues(appContext, R.xml.firefds_kit_settings, true);
        if (forceDefault) {
            Editor editor = sharedPreferences.edit();

            editor.putInt(PREF_SCREEN_TIMEOUT_SECONDS, 30).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_MINUTES, 0).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_HOURS, 0).apply();

        }
        if ((!sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, false)) || forceDefault) {
            Editor editor = sharedPreferences.edit();

            editor.putBoolean(PREF_DISABLE_NUMBER_FORMATTING,
                    SemCscFeature.getInstance()
                            .getBoolean(DISABLE_PHONE_NUMBER_FORMATTING)).apply();
            editor.putBoolean(PREF_DISABLE_SMS_TO_MMS,
                    SemCscFeature.getInstance()
                            .getBoolean(DISABLE_SMS_TO_MMS_CONVERSION_BY_TEXT_INPUT)).apply();
            editor.putBoolean(PREF_FORCE_MMS_CONNECT,
                    SemCscFeature.getInstance()
                            .getBoolean(FORCE_CONNECT_MMS)).apply();
        }
        fixPermissions();
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

    public static String getPreferenceDir() {
        if (mPreferenceDir == null) {
            try {
                SharedPreferences prefs = appContext.getSharedPreferences("dummy", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("dummy", false).apply();
                Field field = prefs.getClass().getDeclaredField("mFile");
                field.setAccessible(true);
                mPreferenceDir = new File(((File) field.get(prefs)).getParent()).getAbsolutePath();
                Log.d("FFK", "Preference folder: " + mPreferenceDir);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Log.e("FFK", "Could not determine preference folder path. Returning default.");
                mPreferenceDir = appContext.getDataDir().getAbsolutePath() + "/shared_prefs";
            }
        }
        return mPreferenceDir;
    }

    private static class RestoreBackupTask extends AsyncTask<Void, Void, Void> {

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
            HashMap<?, ?> entries;
            try {
                FileInputStream fis = new FileInputStream((backup));
                ObjectInputStream input = new ObjectInputStream(fis);
                entries = (HashMap) input.readObject();
                fis.close();
                input.close();
            } catch (IOException | ClassNotFoundException e) {
                log(e);
                return null;
            }
            Editor prefEdit = sharedPreferences.edit();
            prefEdit.clear();
            entries.forEach((key, value) -> {
                if (value instanceof Boolean)
                    prefEdit.putBoolean((String) key, (Boolean) value);
                else if (value instanceof Float)
                    prefEdit.putFloat((String) key, (Float) value);
                else if (value instanceof Integer)
                    prefEdit.putInt((String) key, (Integer) value);
                else if (value instanceof Long)
                    prefEdit.putLong((String) key, (Long) value);
                else if (value instanceof String)
                    prefEdit.putString((String) key, ((String) value));
            });
            prefEdit.apply();
            fixPermissions();

            SystemClock.sleep(1500);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Utils.createSnackbar(activity.findViewById(android.R.id.content),
                    R.string.backup_restored,
                    activity).show();
            RebootNotification.notify(activity, 999, false);
        }
    }
}
