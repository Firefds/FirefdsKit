/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Modifications Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
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

package sb.firefds.xtouchwizS5;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import sb.firefds.xtouchwizS5.adapters.BasicIconListItem;
import sb.firefds.xtouchwizS5.adapters.IIconListAdapterItem;
import sb.firefds.xtouchwizS5.adapters.IconListAdapter;
import sb.firefds.xtouchwizS5.handlers.ScreenRecordHandler;
import sb.firefds.xtouchwizS5.handlers.ScreenshotHandler;
import sb.firefds.xtouchwizS5.utils.Packages;
import sb.firefds.xtouchwizS5.utils.Utils;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XGlobalActions {
	public static final String CLASS_GLOBAL_ACTIONS = "com.android.server.policy.GlobalActions";
	public static final String CLASS_ACTION = "com.android.server.policy.GlobalActions.Action";

	private static Context mContext;
	private static String mRebootStr;
	private static String mRecoveryStr;
	private static String mBootloaderStr;
	private static Drawable mRebootIcon;
	private static Drawable mRecoveryIcon;
	private static Drawable mBootloaderIcon;
	private static Drawable mScreenshotIcon;
	private static Drawable mScreenrecordIcon;
	private static List<IIconListAdapterItem> mRebootItemList;
	private static String mRebootConfirmStr;
	private static String mRebootConfirmRecoveryStr;
	private static String mRebootConfirmBootloaderStr;
	private static String mScreenshotStr;
	private static String mScreenrecordStr;
	private static Unhook mRebootActionHook;
	private static Object mRebootActionItem;
	private static boolean mRebootActionItemStockExists;
	private static Object mScreenshotAction;
	private static Object mScreenrecordAction;
	private static boolean mRebootConfirmRequired;
	private static boolean mScreenshot;
	private static boolean mScreenrecord;
	private static boolean enable4WayReboot;
	private static boolean disablePowerMenuLockscreen;
	private static boolean enableDarkTheme;

	public static void init(final XSharedPreferences prefs, final ClassLoader classLoader) {


		try {
			
			mRebootConfirmRequired = prefs.getBoolean("mRebootConfirmRequired", false);
			mScreenshot = prefs.getBoolean("mScreenshot", true);
			mScreenrecord = prefs.getBoolean("mScreenrecord", true);
			enable4WayReboot = prefs.getBoolean("enable4WayReboot", true);
			disablePowerMenuLockscreen = prefs.getBoolean("disablePowerMenuLockscreen", false);
			enableDarkTheme = prefs.getBoolean("enableDarkTheme", true);

			if (!enable4WayReboot && !disablePowerMenuLockscreen)
				return;

			final Class<?> globalActionsClass = XposedHelpers.findClass(CLASS_GLOBAL_ACTIONS, classLoader);
			final Class<?> actionClass = XposedHelpers.findClass(CLASS_ACTION, classLoader);

			if (disablePowerMenuLockscreen && !enable4WayReboot) 
				disableMenuLockscreen(globalActionsClass);
			else
			{
				XposedBridge.hookAllConstructors(globalActionsClass, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(final MethodHookParam param) throws Throwable {

						// Disable the new Samsung power menu UI
						XposedHelpers.setStaticBooleanField(globalActionsClass, "mNewFeatureForM", false);

						mContext = (Context) param.args[0];
						Context gbContext = mContext.createPackageContext(Packages.XTOUCHWIZ,
								Context.CONTEXT_IGNORE_SECURITY);
						Resources gbRes = gbContext.getResources();

						int recoveryStrId = R.string.reboot_recovery;
						int bootloaderStrId = R.string.reboot_download;
						mRebootStr = gbRes.getString(R.string.reboot_options);
						mRecoveryStr = gbRes.getString(recoveryStrId);
						mBootloaderStr = gbRes.getString(bootloaderStrId);
						mScreenshotStr = gbRes.getString(R.string.screenshot);
						mScreenrecordStr = gbRes.getString(R.string.action_screenrecord);

						// Set the icons appropriately
						// 1st level icons
						Theme theme = gbRes.newTheme();
						mRebootIcon = gbRes.getDrawable(R.drawable.ic_lock_reboot, theme);
						mRecoveryIcon = gbRes.getDrawable(R.drawable.ic_lock_recovery, theme);
						mBootloaderIcon = gbRes.getDrawable(R.drawable.ic_lock_reboot_bootloader, theme);
						mScreenshotIcon = gbRes.getDrawable(R.drawable.ic_screenshot, theme);
						mScreenrecordIcon = gbRes.getDrawable(R.drawable.ic_lock_screen_record, theme);

						mRebootItemList = new ArrayList<IIconListAdapterItem>();
						if (!Locale.getDefault().getLanguage().equals("iw")) {
							mRebootItemList.add(new BasicIconListItem(gbRes.getString(R.string.reboot), null, mRebootIcon, null));
							mRebootItemList.add(new BasicIconListItem(mRecoveryStr, null, mRecoveryIcon, null));
							mRebootItemList.add(new BasicIconListItem(mBootloaderStr, null, mBootloaderIcon, null));
						}
						else {
							mRebootItemList.add(new BasicIconListItem(gbRes.getString(R.string.reboot), null, null, mRebootIcon));
							mRebootItemList.add(new BasicIconListItem(mRecoveryStr, null, null, mRecoveryIcon));
							mRebootItemList.add(new BasicIconListItem(mBootloaderStr, null, null, mBootloaderIcon));			
						}

						mRebootConfirmStr = gbRes.getString(R.string.reboot_confirm);
						mRebootConfirmRecoveryStr = gbRes.getString(R.string.reboot_confirm_recovery);
						mRebootConfirmBootloaderStr = gbRes.getString(R.string.reboot_confirm_bootloader);

					}
				});

				// Force Material theme, Samsung did some regression on the default
				// (old) power menu theme
				int menuTheme;
				if (enableDarkTheme)
					menuTheme=android.R.style.Theme_Material_Dialog_NoActionBar;
				else
					menuTheme=android.R.style.Theme_Material_Light_Dialog_NoActionBar;

				XposedHelpers.findAndHookMethod(CLASS_GLOBAL_ACTIONS + "$GlobalActionsDialog", classLoader,
						"getDialogTheme", Context.class,
						XC_MethodReplacement.returnConstant(menuTheme));

				XposedHelpers.findAndHookMethod(globalActionsClass, "createDialog", new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {

						if (mRebootActionHook != null) {
							mRebootActionHook.unhook();
							mRebootActionHook = null;
						}

					}

					@Override
					protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
						if (mContext == null)
							return;

						@SuppressWarnings("unchecked")
						List<Object> mItems = (List<Object>) XposedHelpers.getObjectField(param.thisObject, "mItems");
						BaseAdapter mAdapter = (BaseAdapter) XposedHelpers.getObjectField(param.thisObject, "mAdapter");
						int index = 1;

						// try to find out if reboot action item already exists
						// in
						// the list of GlobalActions items
						// strategy:
						// 1) check if Action has mIconResId field or
						// mMessageResId
						// field
						// 2) check if the name of the corresponding resource
						// contains "reboot" or "restart" substring
						if (mRebootActionItem == null) {
							Resources res = mContext.getResources();
							for (Object o : mItems) {
								// search for drawable
								try {
									Field f = XposedHelpers.findField(o.getClass(), "mIconResId");
									String resName = res.getResourceEntryName((Integer) f.get(o)).toLowerCase(Locale.US);
									if (resName.contains("reboot") || resName.contains("restart")) {
										mRebootActionItem = o;
										break;
									}
								} catch (NoSuchFieldError nfe) {
									// continue
								} catch (Resources.NotFoundException resnfe) {
									// continue
								} catch (IllegalArgumentException iae) {
									// continue
								}

								if (mRebootActionItem == null) {
									// search for text
									try {
										Field f = XposedHelpers.findField(o.getClass(), "mMessageResId");
										String resName = res.getResourceEntryName((Integer) f.get(o))
												.toLowerCase(Locale.US);
										if (resName.contains("reboot") || resName.contains("restart")) {
											mRebootActionItem = o;
											break;
										}
									} catch (NoSuchFieldError nfe) {
										// continue
									} catch (Resources.NotFoundException resnfe) {
										// continue
									} catch (IllegalArgumentException iae) {
										// continue
									}
								}
							}

							if (mRebootActionItem == null) {
								mRebootActionItemStockExists = false;
								mRebootActionItem = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
										new RebootAction());
							} else {
								mRebootActionItemStockExists = true;
							}
						}

						if (enable4WayReboot == true) {
							// Add/hook reboot action if enabled
							if (mRebootActionItemStockExists) {
								mRebootActionHook = XposedHelpers.findAndHookMethod(mRebootActionItem.getClass(),
										"onPress", new XC_MethodReplacement() {
									@Override
									protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
										RebootAction.showRebootDialog(mContext);
										return null;
									}
								});
							} else {
								// add to the second position
								mItems.add(index, mRebootActionItem);
							}
							index++;
						}

						// Add screenshot action if enabled
						if (mScreenshot == true) {
							if (mScreenshotAction == null) {
								mScreenshotAction = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
										new ScreenshotHandler(mContext, mScreenshotStr, mScreenshotIcon));
							}
							mItems.add(index++, mScreenshotAction);
						}

						if (mScreenrecord == true) {
							// Add screenrecord action if enabled
							if (mScreenrecordAction == null) {
								mScreenrecordAction = Proxy.newProxyInstance(classLoader, new Class<?>[] { actionClass },
										new ScreenRecordHandler(mContext, mScreenrecordStr, mScreenrecordIcon));
							}
							mItems.add(index++, mScreenrecordAction);
						}

						mAdapter.notifyDataSetChanged();
					}
				});
				if (disablePowerMenuLockscreen)
					disableMenuLockscreen(globalActionsClass);
			} 
		}
		catch (Throwable t) {
			XposedBridge.log(t);
		}
	}

	private static void disableMenuLockscreen (Class<?> globalActionsClass) {
		XposedHelpers.findAndHookMethod(globalActionsClass, "showDialog", 
				boolean.class, boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
				boolean locked = (Boolean) param.args[0];
				if (!locked) {
					try {
						Context context = (Context) XposedHelpers.getObjectField(
								param.thisObject, "mContext");
						KeyguardManager km = (KeyguardManager) context.getSystemService(
								Context.KEYGUARD_SERVICE);
						locked = km.isKeyguardLocked();
					} catch (Throwable t) { }
				}

				if (locked) {
					Dialog d = (Dialog) XposedHelpers.getObjectField(param.thisObject, "mDialog");
					if (d == null) {
						XposedHelpers.callMethod(param.thisObject, "createDialog");
					}
					param.setResult(null);
				}
			}
		});
	}

	private static class RebootAction implements InvocationHandler {
		private Context mContext;

		public RebootAction() {
		}

		public static void showRebootDialog(final Context context) {
			if (context == null) {
				return;
			}

			try {

				AlertDialog.Builder builder = new AlertDialog.Builder(context,
						android.R.style.Theme_Material_Light_Dialog_NoActionBar)
						.setTitle(mRebootStr)
						.setAdapter(new IconListAdapter(context, mRebootItemList),
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								handleReboot(context, mRebootStr, which);
							}
						}).setCancelable(true);
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
				dialog.show();
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		private static void doReboot(Context context, int mode) {
			if (mode == 0) {
				Utils.rebootSystem(context, null);
			} else if (mode == 1) {
				Utils.rebootSystem(context, "recovery");
			} else if (mode == 2) {
				Utils.rebootSystem(context, "download");
			}
		}

		private static void handleReboot(final Context context, String caption, final int mode) {
			try {
				if (!mRebootConfirmRequired) {
					doReboot(context, mode);
				} else {
					String message = mRebootConfirmStr;
					if (mode == 1) {
						message = mRebootConfirmRecoveryStr;
					} else if (mode == 2) {
						message = mRebootConfirmBootloaderStr;
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(caption)
							.setMessage(message)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									doReboot(context, mode);
								}
							}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
					AlertDialog dialog = builder.create();
					dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
					dialog.show();
				}
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();

			if (methodName.equals("create")) {
				mContext = (Context) args[0];
				Resources res = mContext.getResources();
				LayoutInflater li = (LayoutInflater) args[3];
				int layoutId = res.getIdentifier("global_actions_item", "layout", "android");
				View v = li.inflate(layoutId, (ViewGroup) args[2], false);

				ImageView icon = (ImageView) v.findViewById(res.getIdentifier("icon", "id", "android"));
				icon.setImageDrawable(mRebootIcon);

				TextView messageView = (TextView) v.findViewById(res.getIdentifier("message", "id", "android"));
				messageView.setText(mRebootStr);

				TextView statusView = (TextView) v.findViewById(res.getIdentifier("status", "id", "android"));
				statusView.setVisibility(View.GONE);

				return v;
			} else if (methodName.equals("onPress")) {
				showRebootDialog(mContext);
				return null;
			} else if (methodName.equals("onLongPress")) {
				handleReboot(mContext, mRebootStr, 0);
				return true;
			} else if (methodName.equals("showDuringKeyguard")) {
				return true;
			} else if (methodName.equals("showBeforeProvisioning")) {
				return true;
			} else if (methodName.equals("isEnabled")) {
				return true;
			} else if (methodName.equals("showConditional")) {
				return true;
			} else {
				return null;
			}
		}
	}
}