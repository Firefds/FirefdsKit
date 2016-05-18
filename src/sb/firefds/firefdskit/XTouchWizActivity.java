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
package sb.firefds.firefdskit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;
import de.robv.android.xposed.library.ui.TextViewPreference;
import eu.chainfire.libsuperuser.Shell;
import sb.firefds.firefdskit.dialogs.CreditsDialog;
import sb.firefds.firefdskit.dialogs.KillWhiteListDialog;
import sb.firefds.firefdskit.dialogs.MultiWindowAppsDialog;
import sb.firefds.firefdskit.dialogs.RestoreDialog;
import sb.firefds.firefdskit.dialogs.SaveDialog;
import sb.firefds.firefdskit.dialogs.RestoreDialog.RestoreDialogListener;
import sb.firefds.firefdskit.notifications.RebootNotification;
import sb.firefds.firefdskit.utils.Utils;

public class XTouchWizActivity extends Activity implements RestoreDialogListener {

	private static ProgressDialog mDialog;

	private static final String[] defaultSettings = new String[] {"addBrowserTerminateButton",
			"enableCameraDuringCall", "disableNumberFormating", "enableCallButtonLogs", "disableSmsToMmsConversion",
			"isXTouvhWizFirstLaunch", "expandNotifications", "makeMeTooLegit", "disableTIMA", "showDataUsuage",
			"autoExpandVolumePanel", "semiTransparentVolumePanel", "enable4WayReboot", "mScreenshot", "mScreenrecord",
			"disableLoudVolumeWarning", "disableSFinderQConnect", "disablePowerMenuLockscreen","disableBatteryCover",
			"disableUSBCover", "hideBatteryIcon","selectedBatteryIcon","disableAirplaneModeOffDialog","showDataPopUp",
			"hideCarrierLabel","customCarrierLabel","carrierSize","hideHeadsetAppsNotification","enableDarkTheme",
			"enableMarshmallowSystemUI","transitionEffect"};

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = { Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE };

	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
		}
	}

	@SuppressLint("WorldReadableFiles")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		verifyStoragePermissions(this);
		
		initScreen();
		setContentView(R.layout.firefds_main);

		try {
			MainApplication.setWindowsSize(new Point());
			getWindowManager().getDefaultDisplay().getSize(MainApplication.getWindowsSize());

			// SettingsFragment.
			if (savedInstanceState == null)
				getFragmentManager().beginTransaction().replace(R.id.prefs, new SettingsFragment()).commit();

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		initScreen();
		super.onConfigurationChanged(newConfig);

	}

	private void initScreen() {

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Throwable t) {
			// Ignore
		}
	}

	@Override
	public void onBackPressed() {
		try {
			if (!isFinishing()) {
				mDialog = new ProgressDialog(this);
				mDialog.setCancelable(false);
				mDialog.setMessage(getString(R.string.exiting_the_application_));
				mDialog.show();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		new QuitTask().execute(this);
	}

	@Override
	protected void onDestroy() {
		try {
			if (mDialog != null) {
				mDialog.dismiss();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	private static class QuitTask extends AsyncTask<Activity, Void, Void> {
		private Activity mActivity = null;

		protected Void doInBackground(Activity... params) {

			try {
				mActivity = (Activity) params[0];

				XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());
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
		return true;

	}

	public boolean ShowRecommendedSettingsDiag() {
		AlertDialog.Builder builder = new AlertDialog.Builder(XTouchWizActivity.this);
		builder.setCancelable(true).setTitle(R.string.app_name).setMessage(R.string.set_recommended_settings)
		.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		}).setPositiveButton(R.string.apply, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				restoreRecommendedSettings();

			}
		}).create().show();

		return true;
	}

	public void restoreRecommendedSettings() {

		MainApplication.getSharedPreferences().edit().clear().commit();
		PreferenceManager.setDefaultValues(this, R.xml.firefds_settings, false);

		Editor editor = MainApplication.getSharedPreferences().edit();

		for (String defaultSetting : defaultSettings) {
			editor.putBoolean(defaultSetting, true).commit();
		}

		editor.putInt("notificationSize", MainApplication.getWindowsSize().x).commit();

		editor.putString("screenOffEffect", "0").commit();
		editor.putString("clockPosition", "Center").commit();

		Toast.makeText(this, R.string.recommended_restored, Toast.LENGTH_SHORT).show();

		XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());

		RebootNotification.notify(this, 999, false);

		recreate();

	}

	@Override
	public void onRestoreDefaults() {

		MainApplication.getSharedPreferences().edit().clear().commit();
		PreferenceManager.setDefaultValues(this, R.xml.firefds_settings, false);

		Toast.makeText(this, R.string.defaults_restored, Toast.LENGTH_SHORT).show();

		MainApplication.getSharedPreferences().edit().putInt("notificationSize", MainApplication.getWindowsSize().x)
		.commit();

		XCscFeaturesManager.applyCscFeatures(MainApplication.getSharedPreferences());

		recreate();

		RebootNotification.notify(this, 999, false);
	}

	@Override
	public void onRestoreBackup(final File backup) {
		new RestoreBackupTask(backup).execute();
	}

	class RestoreBackupTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;
		private File backup;

		public RestoreBackupTask(File backup) {
			this.backup = backup;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(XTouchWizActivity.this);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage(getString(R.string.restoring_backup));
			progressDialog.show();
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
						prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
					else if (v instanceof Float)
						prefEdit.putFloat(key, ((Float) v).floatValue());
					else if (v instanceof Integer)
						prefEdit.putInt(key, ((Integer) v).intValue());
					else if (v instanceof Long)
						prefEdit.putLong(key, ((Long) v).longValue());
					else if (v instanceof String)
						prefEdit.putString(key, ((String) v));
				}
				prefEdit.commit();
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
			progressDialog.dismiss();
			Toast.makeText(XTouchWizActivity.this, R.string.backup_restored, Toast.LENGTH_SHORT).show();
			RebootNotification.notify(XTouchWizActivity.this, 999, false);
		}

	}

	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		private static Context mContext;

		// Fields
		private List<String> changesMade;
		private static Resources res;
		private AlertDialog alertDialog;
		private static ProgressDialog mDialog;
		public static CheckBoxPreference quickPinPref;

		private static Runnable delayedRoot = new Runnable() {

			@Override
			public void run() {
				try {
					if (mDialog != null) {
						mDialog.dismiss();
						Toast.makeText(mContext, R.string.root_info, Toast.LENGTH_LONG).show();
					}

				} catch (Throwable e) {
					e.printStackTrace();
				}

			}
		};

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
			getPreferenceManager().setSharedPreferencesName(XTouchWizActivity.class.getSimpleName());
			MainApplication.setSharedPreferences(getActivity().getPreferences(Context.MODE_WORLD_READABLE));


			try {

				changesMade = new ArrayList<String>();
				mContext = getActivity();

				res = getResources();

				addPreferencesFromResource(R.xml.firefds_settings);

				showDiag();

				MainApplication.getSharedPreferences().edit()
				.putInt("notificationSize", MainApplication.getWindowsSize().x).commit();


				if (!Utils.isSamsungRom()) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
					alertDialogBuilder.setTitle(res.getString(R.string.samsung_rom_warning));

					alertDialogBuilder.setMessage(res.getString(R.string.samsung_rom_warning_msg)).setCancelable(false)
					.setPositiveButton(res.getString(R.string.ok_btn), null);

					alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				}

				findPreference("enableLongBackKillWhiteList").setOnPreferenceClickListener(
						new OnPreferenceClickListener() {
							@Override
							public boolean onPreferenceClick(Preference preference) {
								new KillWhiteListDialog().show(getFragmentManager(), "killWhiteList");
								return true;
							}
						});

				findPreference("selectMwApps").setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						new MultiWindowAppsDialog().show(getFragmentManager(), "multiWindowApps");
						return true;
					}
				});

				TextViewPreference textViewInformationHeader;
				PreferenceScreen ps = (PreferenceScreen) findPreference("prefsRoot");
				textViewInformationHeader = (TextViewPreference) findPreference("xtHeader");
				textViewInformationHeader.setTitle("");

				if (!XposedChecker.isActive()) {
					textViewInformationHeader.setTitle(R.string.firefds_kit_is_not_active);
					textViewInformationHeader.getTextView().setTextColor(Color.RED);
					ps.findPreference("xtHeader").setEnabled(false);
				} else {
					ps.removePreference(textViewInformationHeader);
				}

				new CheckRootTask().execute();

			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		private void showRootDisclaimer() {
			if (mContext != null) {
				try {

					if (mDialog != null) {
						mDialog.dismiss();
					}

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

					alertDialogBuilder.setTitle(R.string.app_name);

					alertDialogBuilder.setMessage(R.string.root_info)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).setCancelable(true);

					alertDialog = alertDialogBuilder.create();
					alertDialog.show();

				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		private void showDiag() {
			mDialog = new ProgressDialog(getActivity());
			mDialog.setMessage(getString(R.string.checking_root_access));
			mDialog.setCancelable(false);
			mDialog.show();
			showDelayedRootMsg();
		}

		private void showDelayedRootMsg() {

			MainApplication.getHandler().postDelayed(delayedRoot, 20000);

		}

		@Override
		public void onDestroy() {
			try {
				if (mDialog != null && mDialog.isShowing()) {
					mDialog.cancel();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			super.onDestroy();
		}

		protected void showDonateAlert() {

			try {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

				alertDialogBuilder.setTitle(R.string.support_app);

				alertDialogBuilder.setMessage(res.getString(R.string.note_please_consider_making_a_donation))
				.setCancelable(true)
				.setPositiveButton(R.string.rate_app, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
						Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
						mContext.startActivity(goToMarket);
					}
				}).setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		private class CheckRootTask extends AsyncTask<Void, Void, Void> {
			private boolean suAvailable = false;

			protected Void doInBackground(Void... params) {
				try {
					suAvailable = Shell.SU.available();

				} catch (Throwable e) {
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(Void p) {

				try {
					MainApplication.getHandler().removeCallbacks(delayedRoot);
					if (mDialog != null) {
						mDialog.dismiss();
					}
					// Check for root access
					if (!suAvailable) {
						showRootDisclaimer();
					} else {
						mDialog.setMessage(res.getString(R.string.loading_application_preferences_));
						if (!mDialog.isShowing()) {
							mDialog.show();
						}
						new CopyCSCTask().execute(mContext);

						if (!MainApplication.getSharedPreferences().getBoolean("isXTouvhWizFirstLaunch", false)) {
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setCancelable(true).setTitle(R.string.app_name)
							.setMessage(R.string.firefds_xposed_disclaimer)
							.setPositiveButton(R.string.ok_btn, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							}).setIcon(android.R.drawable.ic_dialog_alert).create().show();
							MainApplication.getSharedPreferences().edit().putBoolean("isXTouvhWizFirstLaunch", true)
							.commit();
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
					XCscFeaturesManager.getDefaultCSCFeatures();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					XCscFeaturesManager.getDefaultCSCFeaturesFromFiles();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				try {
					Utils.createCSCFiles(mContext);
					if (mDialog != null && mDialog.isShowing()) {
						mDialog.dismiss();
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
		}

		@Override
		public void onPause() {
			super.onPause();
			unregisterPrefsReceiver();
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
				String[] litePrefs = new String[] { "appChooserShowAllActivities", "drt", "drt_ts",
						"isXTouvhWizFirstLaunch", "forceEnglish", "notificationSize", "autoExpandVolumePanel",
				"semiTransparentVolumePanel"};
				for (String string : litePrefs) {
					if (key.equalsIgnoreCase(string)) {
						return;
					}
				}

				// Add preference key to changed keys list
				if (!changesMade.contains(key)) {
					changesMade.add(key);
				}

				RebootNotification.notify(getActivity(), changesMade.size(), true);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}

	}

}
