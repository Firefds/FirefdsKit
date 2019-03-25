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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import sb.firefds.pie.firefdskit.dialogs.CreditDialog;
import sb.firefds.pie.firefdskit.dialogs.RestoreDialog;
import sb.firefds.pie.firefdskit.dialogs.SaveDialog;
import sb.firefds.pie.firefdskit.notifications.RebootNotification;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.PREFS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_ON_RIGHT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CLOCK_DATE_PREFERENCE;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DATA_ICON_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_NUMBER_FORMATTING;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_IS_FIREFDS_KIT_FIRST_LAUNCH;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_MESSAGING_KEY;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NAVIGATION_BAR_COLOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NFC_BEHAVIOR;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_NOTIFICATION_SIZE;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_PHONE_KEY_CAT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ROOT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_HOURS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_MINUTES;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SCREEN_TIMEOUT_SECONDS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF__FAKE_HEADER;

public class FirefdsKitActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        RestoreDialog.RestoreDialogListener {

    @SuppressLint("StaticFieldLeak")
    private static ConstraintLayout mLayout;
    @SuppressLint("StaticFieldLeak")
    private static TextView progressBarText;

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
        if (!(getVisibleFragment() instanceof SettingsFragment)) {
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
        } else {
            new QuitTask().execute(this);
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(pref.getTitle());
        }
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
    public void onRestoreDefaults() {

        MainApplication.getSharedPreferences().edit().clear().apply();
        setDefaultPreferences(this, true);

        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());
        }

        recreate();
        Toast.makeText(this, R.string.defaults_restored, Toast.LENGTH_LONG).show();
        RebootNotification.notify(this, 999, false);
    }

    @Override
    public void onRestoreBackup(final File backup) {
        new RestoreBackupTask(backup).execute();
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
        creditDialog.getDialog(this).show();
    }

    private void showRecommendedSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        saveDialog.showDialog(this, findViewById(android.R.id.content));
    }

    private void showRestoreDialog() {
        RestoreDialog restoreDialog = new RestoreDialog(this);
        restoreDialog.showDialog(this);
    }

    private void restoreRecommendedSettings() {

        MainApplication.getSharedPreferences().edit().clear().apply();
        setDefaultPreferences(this, true);

        if (!Utils.isOmcEncryptedFlag()) {
            XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());
        }

        recreate();
        Toast.makeText(this, R.string.recommended_restored, Toast.LENGTH_LONG).show();
        RebootNotification.notify(this, 999, false);
    }

    private static void setDefaultPreferences(Context context, boolean forceDefault) {
        upgradePreferences();
        PreferenceManager.setDefaultValues(context, R.xml.lockscreen_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.messaging_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.notification_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.phone_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.security_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.sound_settings, true);
        PreferenceManager.setDefaultValues(context, R.xml.system_settings, true);
        if (forceDefault) {
            Editor editor = MainApplication.getSharedPreferences().edit();

            editor.putInt(PREF_NOTIFICATION_SIZE, MainApplication.getWindowsSize().x);

            editor.putInt(PREF_SCREEN_TIMEOUT_SECONDS, 30).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_MINUTES, 0).apply();
            editor.putInt(PREF_SCREEN_TIMEOUT_HOURS, 0).apply();

            editor.putInt(PREF_NAVIGATION_BAR_COLOR,
                    context.getResources()
                            .getIntArray(R.array.navigationbar_color_values)[1]).apply();
        }
        fixPermissions(context);
    }

    private static void upgradePreferences() {
        SharedPreferences preferences = MainApplication.getSharedPreferences();
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

    private Fragment getVisibleFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }

    private static class QuitTask extends AsyncTask<AppCompatActivity, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        private AppCompatActivity mActivity = null;

        @Override
        protected Void doInBackground(AppCompatActivity... appCompatActivities) {
            try {
                mActivity = appCompatActivities[0];
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

    @SuppressLint("StaticFieldLeak")
    private class RestoreBackupTask extends AsyncTask<Void, Void, Void> {

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
            Utils.createSnackbar(findViewById(android.R.id.content),
                    R.string.backup_restored,
                    FirefdsKitActivity.this).show();
            RebootNotification.notify(FirefdsKitActivity.this, 999, false);
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                TextView tv = new TextView(getContext());
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

    public static class SettingsFragment extends FirefdsPreferenceFragment {

        private static Resources res;
        private AlertDialog alertDialog;
        private Context mContext;
        private FragmentActivity activityCompat;

        private Runnable delayedRoot = new Runnable() {

            @Override
            public void run() {
                try {
                    mLayout.setVisibility(View.INVISIBLE);
                    Utils.createSnackbar(activityCompat.findViewById(android.R.id.content),
                            R.string.root_info_short,
                            activityCompat).show();

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            try {
                mContext = getContext();
                activityCompat = getActivity();
                res = getResources();
                SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFS, 0);
                MainApplication.setSharedPreferences(sharedPreferences);
                setDefaultPreferences(mContext, false);

                if (Utils.isOmcEncryptedFlag()) {
                    PreferenceScreen rootScreen = findPreference(PREF_ROOT);
                    PreferenceScreen messagingScreen = findPreference(PREF_MESSAGING_KEY);

                    if (rootScreen != null) {
                        if (messagingScreen != null) {
                            rootScreen.removePreference(messagingScreen);
                        }
                    }
                }

                showProgressBar();

                Editor editor = sharedPreferences.edit();
                editor.putInt(PREF_NOTIFICATION_SIZE, MainApplication.getWindowsSize().x).apply();
                ContentResolver contentResolver = MainApplication.getAppContext().getContentResolver();

                if (sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_HOURS, 0) == 0 &&
                        sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_MINUTES, 0) == 0 &&
                        sharedPreferences.getInt(PREF_SCREEN_TIMEOUT_SECONDS, 0) == 0) {

                    int screenTimeout = Settings.System
                            .getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT);
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
                                Settings.Global.getInt(contentResolver, "navigationbar_color"))
                                .apply();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                fixPermissions(getFragmentContext());

                if (!Utils.isSamsungRom()) {
                    AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(activityCompat);
                    alertDialogBuilder.setTitle(res.getString(R.string.samsung_rom_warning));

                    alertDialogBuilder.setMessage(res.getString(R.string.samsung_rom_warning_msg))
                            .setCancelable(false)
                            .setPositiveButton(res.getString(R.string.ok_btn), null);

                    alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                PreferenceScreen ps = findPreference(PREF_ROOT);
                Preference textViewInformationHeader = findPreference(PREF__FAKE_HEADER);
                if (textViewInformationHeader != null) {
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
                }

                new CheckRootTask().execute();

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
                if (mLayout.getVisibility() == View.VISIBLE) {
                    mLayout.setVisibility(View.INVISIBLE);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            super.onDestroy();
        }

        private void showRootDisclaimer() {
            if (MainApplication.getAppContext() != null) {
                try {

                    mLayout.setVisibility(View.INVISIBLE);

                    AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(activityCompat);

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

        @SuppressLint("StaticFieldLeak")
        private class CheckRootTask extends AsyncTask<Void, Void, Void> {
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
                        new CopyCSCTask().execute(getFragmentContext());

                        if (!MainApplication.getSharedPreferences()
                                .getBoolean(PREF_IS_FIREFDS_KIT_FIRST_LAUNCH, false)) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(activityCompat);
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

        @SuppressLint("StaticFieldLeak")
        private class CopyCSCTask extends AsyncTask<Context, Void, Void> {

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
                        Utils.createCSCFiles(getFragmentContext());
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
    }
}
