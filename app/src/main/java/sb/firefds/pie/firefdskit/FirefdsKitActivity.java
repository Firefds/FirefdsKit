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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Map.Entry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import sb.firefds.pie.firefdskit.dialogs.CreditDialog;
import sb.firefds.pie.firefdskit.dialogs.RestoreDialog;
import sb.firefds.pie.firefdskit.dialogs.SaveDialog;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;
import sb.firefds.pie.firefdskit.utils.Utils;

import static com.android.internal.os.BackgroundThread.getHandler;
import static sb.firefds.pie.firefdskit.utils.Constants.PREFS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_FIRST_LAUNCH;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_MESSAGING_KEY_INDEX;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NAVIGATION_BAR_COLOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NFC_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_PHONE_KEY_CAT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;

public class FirefdsKitActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        RestoreDialog.RestoreDialogListener {

    private static SharedPreferences sharedPreferences;
    private static AppCompatActivity activity;

    private static Runnable delayedRoot = new Runnable() {

        @Override
        public void run() {
            try {
                Utils.createSnackbar(activity.findViewById(android.R.id.content),
                        R.string.root_info_short, activity).show();

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    };

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(PREFS, 0);
        activity = this;
        verifyStoragePermissions(this);

        if (!Utils.isSamsungRom()) {
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Utils.isOmcEncryptedFlag()) {
            navigationView.getMenu().getItem(PREF_MESSAGING_KEY_INDEX).setVisible(false);
        }

        Utils.setOmcEncryptedFlag();
        setDefaultPreferences(false);

        showProgressBar();

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

        fixAppPermissions(this);

        new CheckRootTask().execute();
    }

    @Override
    public void onBackPressed() {
        /*if (!(getVisibleFragment() instanceof MainFragment)) {
            if (getSupportActionBar() != null) {
                if (getVisibleFragment() instanceof ScreenTimeoutSettingsFragment) {
                    getSupportActionBar().setTitle(R.string.system);
                } else {
                    getSupportActionBar().setHomeButtonEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setTitle(R.string.app_name);
                }
            }
            super.onBackPressed();
        } else {*/
        new QuitTask().execute(this);
        //}
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
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(item.getTitle());
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static Context getActivity() {
        return activity;
    }

    public static void verifyStoragePermissions(AppCompatActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }

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
                new File(context.getFilesDir().getParentFile(), "shared_prefs");
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
    public static void fixAppPermissions(Context context) {
        File appFolder = context.getFilesDir().getParentFile();
        appFolder.setExecutable(true, false);
        appFolder.setReadable(true, false);
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

    private static void setDefaultPreferences(boolean forceDefault) {
        upgradePreferences();
        PreferenceManager.setDefaultValues(activity, R.xml.lockscreen_settings, true);
        PreferenceManager.setDefaultValues(activity, R.xml.messaging_settings, true);
        PreferenceManager.setDefaultValues(activity, R.xml.notification_settings, true);
        PreferenceManager.setDefaultValues(activity, R.xml.phone_settings, true);
        PreferenceManager.setDefaultValues(activity, R.xml.security_settings, true);
        PreferenceManager.setDefaultValues(activity, R.xml.sound_settings, true);
        PreferenceManager.setDefaultValues(activity, R.xml.system_settings, true);
        if (forceDefault) {
            Editor editor = sharedPreferences.edit();

            editor.putInt(PREF_SCREEN_TIMEOUT_SECONDS, 30).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_MINUTES, 0).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_HOURS, 0).apply();

            editor.putInt(PREF_NAVIGATION_BAR_COLOR,
                    activity.getResources()
                            .getIntArray(R.array.navigationbar_color_values)[1]).apply();
        }
        fixPermissions(activity);
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
                getHandler().removeCallbacks(delayedRoot);
                // Check for root access
                if (!suAvailable) {
                    showDisclaimer(R.string.root_info, false);
                } else {
                    new CopyCSCTask().execute(getActivity());

                    if (!sharedPreferences.getBoolean(PREF_FIRST_LAUNCH, false)) {
                        showDisclaimer(R.string.firefds_xposed_disclaimer, true);
                        sharedPreferences.edit().putBoolean(PREF_FIRST_LAUNCH, true).apply();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void showDisclaimer(int messageId, boolean showIcon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true)
                .setTitle(R.string.app_name)
                .setMessage(messageId)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());

        if (showIcon) {
            builder.setIcon(android.R.drawable.ic_dialog_alert);
        }

        builder.create().show();
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
                fixPermissions(getApplicationContext());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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

    private void showProgressBar() {
        showDelayedRootMsg();
    }

    private void showDelayedRootMsg() {
        getHandler().postDelayed(delayedRoot, 20000);
    }

    public static class NotificationSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.notification_settings, rootKey);
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

    public static class LockscreenSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.lockscreen_settings, rootKey);
        }
    }

    public static class MessagingSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.messaging_settings, rootKey);
        }
    }

    public static class PhoneSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.phone_settings, rootKey);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (Utils.isOmcEncryptedFlag()) {
                PreferenceCategory phoneScreen = findPreference(PREF_PHONE_KEY_CAT);
                SwitchPreferenceCompat disableNumberFormattingPreference =
                        findPreference(PREF_DISABLE_NUMBER_FORMATTING);

                if (phoneScreen != null) {
                    if (disableNumberFormattingPreference != null) {
                        phoneScreen.removePreference(disableNumberFormattingPreference);
                    }
                }
            }
        }
    }

    public static class SecuritySettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.security_settings, rootKey);
        }
    }

    public static class SoundSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sound_settings, rootKey);
        }
    }

    public static class SystemSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.system_settings, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            super.onSharedPreferenceChanged(sharedPreferences, key);

            if (key.equals(PREF_NAVIGATION_BAR_COLOR)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                TextView tv = new TextView(activity);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(R.string.navigation_bar_color_dialog_message);
                tv.setPadding(16, 16, 16, 16);
                builder.setTitle(R.string.navigation_bar_color_dialog_title)
                        .setView(tv)
                        .setNeutralButton("OK", (dialog, id) -> dialog.dismiss())
                        .show();
            }
        }
    }

    public static class ScreenTimeoutSettingsFragment extends FirefdsPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.screen_timeout_settings, rootKey);
        }
    }

    /*public static class MainFragment extends FirefdsPreferenceFragment {

        private static Resources res;
        private AlertDialog alertDialog;
        //private Context mContext;
        private FragmentActivity activityCompat;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            try {
                //mContext = getActivity();
                activityCompat = getActivity();
                res = getResources();
                //setDefaultPreferences(context, false);



                *//*if (textViewInformationHeader != null) {
                    textViewInformationHeader.setTitle("");
                }

                if (!XposedChecker.isActive()) {
                    if (textViewInformationHeader != null) {
                        textViewInformationHeader.setTitle(R.string.firefds_kit_is_not_active);
                    }
                    if (textViewInformationHeader != null) {
                        textViewInformationHeader.setEnabled(false);
                    }
                } else {
                    if (ps != null) {
                        if (textViewInformationHeader != null) {
                            ps.removePreference(textViewInformationHeader);
                        }
                    }
                }*//*


            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.firefds_settings, rootKey);
        }

        @Override
        public void onDestroy() {
            try {
//                if (mLayout.getVisibility() == View.VISIBLE) {
//                    mLayout.setVisibility(View.INVISIBLE);
//                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            super.onDestroy();
        }
    }*/
}
