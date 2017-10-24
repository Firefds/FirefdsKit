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

package sb.firefds.nougat.firefdskit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.Unhook;
import sb.firefds.nougat.firefdskit.utils.Packages;
import sb.firefds.nougat.firefdskit.utils.Utils;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XGlobalActions {
	public static final String CLASS_GLOBAL_ACTIONS = "com.android.server.policy.GlobalActions";
	public static final String CLASS_ACTION = "com.android.server.policy.GlobalActions.Action";

	private static Context mContext;
	private static String mRebootConfirmStr;
	private static String mRebootConfirmRecoveryStr;
	private static String mRebootConfirmBootloaderStr;
	private static Unhook mRebootActionHook;
	private static Object mRebootActionItem;
	private static boolean mRebootActionItemStockExists;
	private static boolean mRebootConfirmRequired;
	private static boolean enable4WayReboot;
	private static String[] rebootString;
	private static String rebootMenu;
	private static int rebootMode;

	private static final int REBOOT_MODE_NORMAL = 0;
	private static final int REBOOT_MODE_SOFT = 1;
	private static final int REBOOT_MODE_BOOTLOADER = 2;
	private static final int REBOOT_MODE_RECOVERY = 3;

	public static void init(final XSharedPreferences prefs, final ClassLoader classLoader) {

		try {
			final Class<?> globalActionsClass = XposedHelpers.findClass(CLASS_GLOBAL_ACTIONS, classLoader);
			final Class<?> actionClass = XposedHelpers.findClass(CLASS_ACTION, classLoader);

			//hides reboot confirmation screen
			XposedBridge.hookAllMethods(globalActionsClass, "initValueForCreate", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					prefs.reload();
					if (prefs.getBoolean("enable4WayReboot", false)){
						XposedHelpers.setIntField(param.thisObject, "mRestartIconResId",0);
						XposedHelpers.setIntField(param.thisObject, "mConfirmRestartIconResId",0);
					}
				}
			});

			//hooks constructors and sets resources
			XposedBridge.hookAllConstructors(globalActionsClass, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {

					mContext = (Context) param.args[0];
					Context gbContext = mContext.createPackageContext(Packages.FIREFDSKIT,
							Context.CONTEXT_IGNORE_SECURITY);
					Resources gbRes = gbContext.getResources();
					rebootString=gbRes.getStringArray(R.array.reboot_options);
					rebootMenu=gbRes.getString(R.string.reboot_options);
					mRebootConfirmStr = gbRes.getString(R.string.reboot_confirm);
					mRebootConfirmRecoveryStr = gbRes.getString(R.string.reboot_confirm_recovery);
					mRebootConfirmBootloaderStr = gbRes.getString(R.string.reboot_confirm_bootloader);
				}
			});

			XposedHelpers.findAndHookMethod(globalActionsClass, "createDialog", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					if (mRebootActionHook != null) {
						mRebootActionHook.unhook();
						mRebootActionHook = null;
					}
				}
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					prefs.reload();
					enable4WayReboot = prefs.getBoolean("enable4WayReboot", false);
					mRebootConfirmRequired = prefs.getBoolean("mRebootConfirmRequired", false);

					@SuppressWarnings("unchecked")
					List<Object> mItems = (List<Object>) XposedHelpers.getObjectField(param.thisObject, "mItems");
					BaseAdapter mAdapter = (BaseAdapter) XposedHelpers.getObjectField(param.thisObject, "mAdapter");
					int index = 1;

					// try to find out if reboot action item already exists in the list of GlobalActions items
					// strategy:
					// 1) check if Action has mIconResId field or mMessageResId field
					// 2) check if the name of the corresponding resource contains "reboot" or "restart" substring
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

					if (enable4WayReboot) {
						// Add/hook reboot action if enabled
						if (mRebootActionItemStockExists) {
							mItems.add(++index, mRebootActionItem);
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
					mAdapter.notifyDataSetChanged();
				}
			});

			XC_MethodHook showDialogHook = new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
					prefs.reload();
					if (prefs.getBoolean("disablePowerMenuLockscreen", false)) {
						boolean locked = (Boolean) param.args[0];
						if (!locked) {
							// double-check using keyguard manager
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
				}
			};
			XposedHelpers.findAndHookMethod(globalActionsClass, "showDialog", 
					boolean.class, boolean.class, showDialogHook);
		}
		catch (Exception e) {
		}
	}

	private static class RebootAction implements InvocationHandler {
		private Context mContext;

		public static void showRebootDialog(final Context context) {
			if (context == null) {
				return;
			}
			try {
				AlertDialog.Builder builder = new AlertDialog.Builder(context)
						.setTitle(rebootMenu)
						.setSingleChoiceItems(rebootString, -1, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								rebootMode=which;
							};
						})
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								handleReboot(context, rebootMenu, rebootMode);
							};
						})
						.setNegativeButton(android.R.string.no,null);

				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
				dialog.getWindow().setFlags(4, 4);
				dialog.show();
			} catch (Throwable t) {
				XposedBridge.log(t);
			}
		}

		private static void doReboot(final Context context, final int mode) {
			final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if (mode == REBOOT_MODE_NORMAL) {
				pm.reboot(null);
			} else if (mode == REBOOT_MODE_SOFT) {
				Utils.performSoftReboot();
			} else if (mode == REBOOT_MODE_RECOVERY) {
				pm.reboot("recovery");
			} else if (mode == REBOOT_MODE_BOOTLOADER) {
				pm.reboot("download");
			}
		}

		private static void handleReboot(final Context context, String caption, final int mode) {
			try {
				if (!mRebootConfirmRequired) {
					doReboot(context, mode);
				} else {
					String message;
					if (mode == REBOOT_MODE_RECOVERY) {
						message = mRebootConfirmRecoveryStr;
					} else if (mode == REBOOT_MODE_BOOTLOADER) {
						message = mRebootConfirmBootloaderStr;
					}else{
						message = mRebootConfirmStr;
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(caption)
							.setMessage(message)
							.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									doReboot(context, mode);
								}
							})
							.setNegativeButton(android.R.string.no,null);
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

				TextView messageView = (TextView) v.findViewById(res.getIdentifier("message", "id", "android"));
				messageView.setText(rebootMenu);

				TextView statusView = (TextView) v.findViewById(res.getIdentifier("status", "id", "android"));
				statusView.setVisibility(View.GONE);

				return v;
			} else if (methodName.equals("onPress")) {
				showRebootDialog(mContext);
				return null;
			} else if (methodName.equals("onLongPress")) {
				handleReboot(mContext, rebootMenu, REBOOT_MODE_NORMAL);
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