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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import de.robv.android.xposed.library.ui.TextViewPreference;

import com.topjohnwu.superuser.Shell;

import sb.firefds.pie.firefdskit.dialogs.CreditsDialog;
import sb.firefds.pie.firefdskit.dialogs.RestoreDialog;
import sb.firefds.pie.firefdskit.dialogs.RestoreDialog.RestoreDialogListener;
import sb.firefds.pie.firefdskit.dialogs.SaveDialog;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.PREFS;
import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class FirefdsKitActivity extends AppCompatActivity implements RestoreDialogListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static RelativeLayout mLayout;
    private static TextView progressBarText;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
    }

    private static final String[] defaultSettings = MainApplication
            .getAppContext()
            .getResources()
            .getStringArray(R.array.default_settings);

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void verifyStoragePermissions(Activity activity) {
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

    @SuppressLint("SetWorldReadable")
    public static void fixAppPermissions(Context context) {
        File appFolder = context.getFilesDir().getParentFile();
        appFolder.setExecutable(true, false);
        appFolder.setReadable(true, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Utils.setOmcEncryptedFlag();
        fixAppPermissions(this);
        verifyStoragePermissions(this);

        setContentView(R.layout.firefds_main);
        mLayout = findViewById(R.id.mainLayout);
        progressBarText = findViewById(R.id.progressBarText);

        try {

            MainApplication.setWindowsSize(new Point());
            getWindowManager().getDefaultDisplay().getSize(MainApplication.getWindowsSize());

            if (savedInstanceState == null)
                getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                        new SettingsFragment()).commit();


        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            new QuitTask().execute(this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment(),
                args);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    private static class QuitTask extends AsyncTask<Activity, Void, Void> {
        private Activity mActivity = null;

        protected Void doInBackground(Activity... params) {

            try {
                mActivity = params[0];
                if (!Utils.isOmcEncryptedFlag()) {
                    XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_credits:
                new CreditsDialog().show(getFragmentManager(), "credits");
                break;
            case R.id.recommended_settings:
                ShowRecommendedSettingsDiag();
                break;
            case R.id.action_save:
                new SaveDialog().show(getFragmentManager(), "save");
                break;
            case R.id.action_restore:
                new RestoreDialog().show(getFragmentManager(), "restore");
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    public void ShowRecommendedSettingsDiag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FirefdsKitActivity.this);
        builder.setCancelable(true)
                .setTitle(R.string.app_name)
                .setMessage(R.string.set_recommended_settings)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.apply, (dialog, which) -> restoreRecommendedSettings())
                .create()
                .show();
    }

    public void restoreRecommendedSettings() {

        MainApplication.getSharedPreferences().edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.firefds_settings, false);

        Editor editor = MainApplication.getSharedPreferences().edit();

        for (String defaultSetting : defaultSettings) {
            editor.putBoolean(defaultSetting, true).apply();
        }

        editor.putInt(PREF_NOTIFICATION_SIZE, MainApplication.getWindowsSize().x).apply();

        fixPermissions(getApplicationContext());

        Toast.makeText(this, R.string.recommended_restored, Toast.LENGTH_SHORT).show();

        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());
        }
        RebootNotification.notify(this, 999, false);

        recreate();

    }

    @Override
    public void onRestoreDefaults() {

        MainApplication.getSharedPreferences().edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.firefds_settings, false);

        Toast.makeText(this, R.string.defaults_restored, Toast.LENGTH_SHORT).show();

        MainApplication.getSharedPreferences()
                .edit()
                .putInt(PREF_NOTIFICATION_SIZE, MainApplication.getWindowsSize().x)
                .apply();

        fixPermissions(getApplicationContext());

        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());
        }

        recreate();

        RebootNotification.notify(this, 999, false);
    }

    @Override
    public void onRestoreBackup(final File backup) {
        new RestoreBackupTask(backup).execute();
    }

    class RestoreBackupTask extends AsyncTask<Void, Void, Void> {

        private File backup;

        RestoreBackupTask(File backup) {
            this.backup = backup;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarText.setText(getString(R.string.restoring_backup));
            mLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ObjectInputStream input = null;
            try {
                input = new ObjectInputStream(new FileInputStream(backup));
                Editor prefEdit = MainApplication.getSharedPreferences().edit();
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
            mLayout.setVisibility(View.INVISIBLE);
            Toast.makeText(FirefdsKitActivity.this, R.string.backup_restored, Toast.LENGTH_SHORT).show();
            RebootNotification.notify(FirefdsKitActivity.this, 999, false);
        }
    }

    public static class NotificationSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.notification_settings, rootKey);
        }
    }

    public static class LockscreenSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.lockscreen_settings, rootKey);
        }
    }

    public static class MessagingSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.messaging_settings, rootKey);
        }
    }

    public static class PhoneSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.phone_settings, rootKey);
        }
    }

    public static class SecuritySettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.security_settings, rootKey);
        }
    }

    public static class SoundSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.sound_settings, rootKey);
        }
    }

    public static class SystemSettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.system_settings, rootKey);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

        private static Context mContext;

        // Fields
        private List<String> changesMade;
        private static Resources res;
        private AlertDialog alertDialog;

        private static Runnable delayedRoot = new Runnable() {

            @Override
            public void run() {
                try {
                    mLayout.setVisibility(View.INVISIBLE);
                    Toast.makeText(mContext, R.string.root_info, Toast.LENGTH_LONG).show();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            try {
                changesMade = new ArrayList<>();
                mContext = Objects.requireNonNull(getActivity()).getBaseContext();

                res = getResources();

                SharedPreferences sharedPreferences
                        = mContext.getSharedPreferences(PREFS, 0);
                MainApplication.setSharedPreferences(sharedPreferences);
                //addPreferencesFromResource(R.xml.firefds_settings);

                if (Utils.isOmcEncryptedFlag()) {
                    androidx.preference.PreferenceScreen rootScreen =
                            findPreference(PREF_ROOT);
                    androidx.preference.PreferenceScreen messagingScreen =
                            findPreference(PREF_MESSAGING_KEY);
                    androidx.preference.PreferenceCategory phoneScreen =
                            findPreference(PREF_PHONE_KEY_CAT);
                    androidx.preference.SwitchPreference disableNumberFormattingPreference =
                            findPreference(PREF_DISABLE_NUMBER_FORMATTING);

                    if (rootScreen != null) {
                        if (messagingScreen != null) {
                            rootScreen.removePreference(messagingScreen);
                        }
                    }
                    if (phoneScreen != null) {
                        if (disableNumberFormattingPreference != null) {
                            phoneScreen.removePreference(disableNumberFormattingPreference);
                        }
                    }
                }

                androidx.preference.ListPreference clock_date_preference =
                        findPreference(PREF_CLOCK_DATE_PREFERENCE);
                androidx.preference.SwitchPreference clockDateOnRight =
                        findPreference(PREF_CLOCK_DATE_ON_RIGHT);
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

                showProgressBar();

                MainApplication.getSharedPreferences().edit()
                        .putInt(PREF_NOTIFICATION_SIZE, MainApplication.getWindowsSize().x).apply();
                fixPermissions(mContext);


                if (!Utils.isSamsungRom()) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(res.getString(R.string.samsung_rom_warning));

                    alertDialogBuilder.setMessage(res.getString(R.string.samsung_rom_warning_msg))
                            .setCancelable(false)
                            .setPositiveButton(res.getString(R.string.ok_btn), null);

                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                //TextViewPreference textViewInformationHeader;
                androidx.preference.PreferenceScreen ps = findPreference(PREF_ROOT);
                Preference textViewInformationHeader = findPreference(PREF__FAKE_HEADER);
                if (textViewInformationHeader != null) {
                    textViewInformationHeader.setTitle("");
                }

                if (!XposedChecker.isActive()) {
                    if (textViewInformationHeader != null) {
                        textViewInformationHeader.setTitle(R.string.firefds_kit_is_not_active);
                    }
                    //textViewInformationHeader.getTextView().setTextColor(Color.RED);
                    if (textViewInformationHeader != null) {
                        textViewInformationHeader.setEnabled(false);
                    }
                } else {
                    if (ps != null) {
                        if (textViewInformationHeader != null) {
                            ps.removePreference(textViewInformationHeader);
                        }
                    }
                }

                new CheckRootTask().execute();

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.firefds_settings);
        }

        private void showRootDisclaimer() {
            if (mContext != null) {
                try {

                    mLayout.setVisibility(View.INVISIBLE);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                    alertDialogBuilder.setTitle(R.string.app_name);

                    alertDialogBuilder.setMessage(R.string.root_info)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                            .setCancelable(true);

                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        private void showProgressBar() {
            progressBarText.setText(res.getString(R.string.checking_root_access));
            mLayout.setVisibility(View.VISIBLE);
            showDelayedRootMsg();
        }

        private void showDelayedRootMsg() {

            MainApplication.getHandler().postDelayed(delayedRoot, 20000);

        }

        @Override
        public void onDestroy() {
            try {
                if (mLayout.getVisibility() == View.VISIBLE) {
                    mLayout.setVisibility(View.INVISIBLE);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            super.onDestroy();
        }

        private class CheckRootTask extends AsyncTask<Void, Void, Void> {
            private boolean suAvailable = false;

            protected Void doInBackground(Void... params) {
                try {
                    suAvailable = Shell.rootAccess();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void p) {

                try {
                    MainApplication.getHandler().removeCallbacks(delayedRoot);
                    mLayout.setVisibility(View.INVISIBLE);
                    // Check for root access
                    if (!suAvailable) {
                        showRootDisclaimer();
                    } else {
                        progressBarText.setText(res.getString(R.string.loading_application_preferences_));
                        if (mLayout.getVisibility() == View.INVISIBLE) {
                            mLayout.setVisibility(View.VISIBLE);
                        }
                        new CopyCSCTask().execute(mContext);

                        if (!MainApplication.getSharedPreferences()
                                .getBoolean(PREF_IS_FIREFDS_KIT_FIRST_LAUNCH, false)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setCancelable(true)
                                    .setTitle(R.string.app_name)
                                    .setMessage(R.string.firefds_xposed_disclaimer)
                                    .setPositiveButton(R.string.ok_btn, (dialog, which) -> dialog.dismiss())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .create()
                                    .show();
                            MainApplication.getSharedPreferences()
                                    .edit()
                                    .putBoolean(PREF_IS_FIREFDS_KIT_FIRST_LAUNCH, true)
                                    .apply();
                        }
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
                        Utils.createCSCFiles(mContext);
                    }
                    if (mLayout.getVisibility() == View.VISIBLE) {
                        mLayout.setVisibility(View.INVISIBLE);
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }
                super.onPostExecute(result);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            registerPrefsReceiver();
            fixPermissions(mContext);
        }

        @Override
        public void onPause() {
            super.onPause();
            unregisterPrefsReceiver();
            fixPermissions(mContext);
        }

        private void registerPrefsReceiver() {
            MainApplication.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        private void unregisterPrefsReceiver() {
            MainApplication.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            try {
                // No reboot notification required
                String[] litePrefs =
                        mContext.getResources().getStringArray(R.array.lite_preferences);

                setTimeoutPrefs(sharedPreferences, key);

                for (String string : litePrefs) {
                    if (key.equalsIgnoreCase(string)) {
                        fixPermissions(mContext);
                        return;
                    }
                }

                // Add preference key to changed keys list
                if (!changesMade.contains(key)) {
                    changesMade.add(key);
                }
                fixPermissions(mContext);
                RebootNotification.notify(getActivity(), changesMade.size(), false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private void setTimeoutPrefs(SharedPreferences sharedPreferences, String key) {

            int timeoutML = 0;

            if (key.equalsIgnoreCase(PREF_SCREEN_TIMEOUT_SECONDS)) {
                timeoutML += sharedPreferences.getInt(key, 30) * 1000;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, timeoutML);
            }
            if (key.equalsIgnoreCase(PREF_SCREEN_TIMEOUT_MINUTES)) {
                timeoutML += sharedPreferences.getInt(key, 0) * 60000;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, timeoutML);
            }
            if (key.equalsIgnoreCase(PREF_SCREEN_TIMEOUT_HOURS)) {
                timeoutML += sharedPreferences.getInt(key, 0) * 3600000;
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, timeoutML);
            }
        }
    }
}
