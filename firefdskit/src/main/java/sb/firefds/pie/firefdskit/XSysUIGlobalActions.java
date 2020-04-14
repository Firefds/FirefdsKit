/*
 * Copyright (C) 2019 Shauli Bracha for FirefdsKit Project (firefds@xda)
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

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.SecGlobalActionsPresenter;
import com.samsung.android.globalactions.presentation.view.ResourceFactory;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModelFactory;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.SystemConditions;
import com.samsung.android.globalactions.util.UtilFactory;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.actionViewModels.FirefdsKitActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.FirefdsKitActionViewModelsFactory;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.DATA_MODE_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.EMERGENCY_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.FLASHLIGHT_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.MULTIUSER_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.POWER_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.RECOVERY_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.RESTART_ACATION;
import static sb.firefds.pie.firefdskit.utils.Constants.RESTART_UI_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.SCREENSHOT_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.SCREEN_RECORD_ACTION;
import static sb.firefds.pie.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_CUSTOM_RECOVERY_CONFIRMATION;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_POWER_MENU_SECURE_LOCKSCREEN;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_DISABLE_RESTART_CONFIRMATION;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_ADVANCED_POWER_MENU;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_DATA_MODE;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_DOWNLOAD;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_EMERGENCY_MODE;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_FLASHLIGHT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_POWER_OFF;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_RECOVERY;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_RESTART;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_RESTART_SYSTEMUI;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_SCREENSHOT;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_ENABLE_SCREEN_RECORD;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_SUPPORTS_MULTIPLE_USERS;
import static sb.firefds.pie.firefdskit.utils.Preferences.PREF_UNLOCK_KEYGUARD_BEFORE_ACTION_EXECUTE;

public class XSysUIGlobalActions {

    private static final String GLOBAL_ACTIONS_PACKAGE = "com.samsung.android.globalactions.presentation";
    private static final String SEC_GLOBAL_ACTIONS_PRESENTER = GLOBAL_ACTIONS_PACKAGE + ".SecGlobalActionsPresenter";
    private static final String DEFAULT_ACTION_VIEW_MODEL_FACTORY = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.DefaultActionViewModelFactory";
    private static final String SEC_GLOBAL_ACTIONS_DIALOG_BASE = GLOBAL_ACTIONS_PACKAGE + ".view.SecGlobalActionsDialogBase";
    private static final String GLOBAL_ACTION_ITEM_VIEW = GLOBAL_ACTIONS_PACKAGE + ".view.GlobalActionItemView";
    private static final String FLASHLIGHT_CONTROLLER_IMPL_CLASS = SYSTEM_UI + ".statusbar.policy.FlashlightControllerImpl";
    private static final String RESTART_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.RestartActionViewModel";
    private static final String SAFE_MODE_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.SafeModeActionViewModel";
    private static final String POWER_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.PowerActionViewModel";
    private static final String EMERGENCY_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.EmergencyActionViewModel";

    private static SecGlobalActionsPresenter mSecGlobalActionsPresenter;
    private static Map<String, Object> actionViewModelDefaults;
    private static Map<String, Drawable> actionIcons = new HashMap<>();
    private static Map<String, String> actionStrings = new HashMap<>();
    private static Object mFlashlightObject;
    private static boolean prefUnlockKeyguardBeforeActionExecute;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        XposedHelpers.findAndHookConstructor(FLASHLIGHT_CONTROLLER_IMPL_CLASS,
                classLoader,
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        mFlashlightObject = param.thisObject;
                    }
                });

        final Class<?> secGlobalActionsDialogBaseClass = XposedHelpers.findClass(SEC_GLOBAL_ACTIONS_DIALOG_BASE, classLoader);
        prefUnlockKeyguardBeforeActionExecute = prefs.getBoolean(PREF_UNLOCK_KEYGUARD_BEFORE_ACTION_EXECUTE, false);

        try {
            if (prefUnlockKeyguardBeforeActionExecute) {
                XC_MethodHook isNeedSecureConfirmHook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        ConditionChecker mConditionChecker =
                                (ConditionChecker) XposedHelpers.getObjectField(param.thisObject, "mConditionChecker");
                        if (mConditionChecker.isEnabled(SystemConditions.IS_SECURE_KEYGUARD)) {
                            param.setResult(Boolean.TRUE);
                        }
                    }
                };

                XposedHelpers.findAndHookMethod(RESTART_ACTION_VIEW_MODEL,
                        classLoader,
                        "isNeedSecureConfirm",
                        isNeedSecureConfirmHook);

                XposedHelpers.findAndHookMethod(SAFE_MODE_ACTION_VIEW_MODEL,
                        classLoader,
                        "isNeedSecureConfirm",
                        isNeedSecureConfirmHook);

                XposedHelpers.findAndHookMethod(POWER_ACTION_VIEW_MODEL,
                        classLoader,
                        "isNeedSecureConfirm",
                        isNeedSecureConfirmHook);

                XposedHelpers.findAndHookMethod(EMERGENCY_ACTION_VIEW_MODEL,
                        classLoader,
                        "isNeedSecureConfirm",
                        isNeedSecureConfirmHook);
            }

            if (prefs.getBoolean(PREF_DISABLE_POWER_MENU_SECURE_LOCKSCREEN, false)) {
                XposedHelpers.findAndHookMethod(secGlobalActionsDialogBaseClass,
                        "showDialog",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                Dialog dialog = (Dialog) XposedHelpers.getObjectField(param.thisObject, "mDialog");
                                KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                                if (mKeyguardManager.isKeyguardLocked()) {
                                    dialog.dismiss();
                                }
                            }
                        }
                );
            }

            if (prefs.getBoolean(PREF_DISABLE_RESTART_CONFIRMATION, false)) {
                XposedHelpers.findAndHookMethod(SEC_GLOBAL_ACTIONS_PRESENTER,
                        classLoader,
                        "isActionConfirming",
                        XC_MethodReplacement.returnConstant(Boolean.TRUE));
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        if (prefs.getBoolean(PREF_ENABLE_ADVANCED_POWER_MENU, false)) {

            try {
                XposedBridge.hookAllConstructors(secGlobalActionsDialogBaseClass,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                                Context ctx = (Context) param.args[0];
                                Resources res = ctx.getResources();
                                Context gbContext = Utils.getGbContext(ctx, res.getConfiguration());

                                actionStrings.put("mRecoveryStr",
                                        prefs.getString(PREF_CUSTOM_RECOVERY, gbContext.getString(R.string.reboot_recovery)));
                                actionStrings.put("mDownloadStr",
                                        gbContext.getString(R.string.reboot_download));
                                actionStrings.put("mScreenshotStr",
                                        gbContext.getString(R.string.screenshot));
                                actionStrings.put("mSwitchUserStr",
                                        gbContext.getString(R.string.switchUser));
                                actionStrings.put("mRestartSystemUiStr",
                                        gbContext.getString(R.string.restartUI));
                                actionStrings.put("mFlashlightStr",
                                        gbContext.getString(R.string.flashlight));
                                actionStrings.put("mScreenRecordStr",
                                        gbContext.getString(R.string.screen_record));
                                actionStrings.put("mRebootConfirmRecoveryStr",
                                        prefs.getString(PREF_CUSTOM_RECOVERY_CONFIRMATION, gbContext.getString(R.string.reboot_confirm_recovery)));
                                actionStrings.put("mRebootConfirmDownloadStr",
                                        gbContext.getString(R.string.reboot_confirm_download));
                                actionStrings.put("mRestartSystemUiConfirmStr",
                                        gbContext.getString(R.string.restartUI));
                                actionStrings.put("mFlashlightOnStr",
                                        gbContext.getString(R.string.flashlight_on));
                                actionStrings.put("mFlashlightOffStr",
                                        gbContext.getString(R.string.flashlight_off));

                                actionIcons.put("mRecoveryIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_recovery_stock));
                                actionIcons.put("mDownloadIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_download_stock));
                                actionIcons.put("mScreenshotIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_screenshot_stock));
                                actionIcons.put("mSwitchUserIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_users_stock));
                                actionIcons.put("mRestartSystemUiIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_restart_ui_stock));
                                actionIcons.put("mFlashLightIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_torchlight_stock));
                                actionIcons.put("mScreenRecordIcon",
                                        gbContext.getDrawable(R.drawable.tw_ic_do_screenrecord_stock));

                            }
                        });

                XposedHelpers.findAndHookMethod(SEC_GLOBAL_ACTIONS_PRESENTER,
                        classLoader,
                        "createDefaultActions",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                ActionViewModelFactory actionViewModelFactory = (ActionViewModelFactory) XposedHelpers
                                        .getObjectField(param.thisObject, "mViewModelFactory");
                                mSecGlobalActionsPresenter = (SecGlobalActionsPresenter) param.thisObject;
                                if (!prefs.getBoolean(PREF_ENABLE_POWER_OFF, true)) {
                                    mSecGlobalActionsPresenter.clearActions(POWER_ACTION);
                                }
                                if (!prefs.getBoolean(PREF_ENABLE_RESTART, true)) {
                                    mSecGlobalActionsPresenter.clearActions(RESTART_ACATION);
                                }
                                if (!prefs.getBoolean(PREF_ENABLE_EMERGENCY_MODE, true)) {
                                    mSecGlobalActionsPresenter.clearActions(EMERGENCY_ACTION);
                                }
                                if (prefs.getBoolean(PREF_ENABLE_RECOVERY, true)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    RECOVERY_ACTION));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_DOWNLOAD, true)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    DOWNLOAD_ACTION));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_DATA_MODE, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    DATA_MODE_ACTION));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_SCREENSHOT, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    SCREENSHOT_ACTION));
                                }
                                if (prefs.getBoolean(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    MULTIUSER_ACTION));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_RESTART_SYSTEMUI, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    RESTART_UI_ACTION));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_FLASHLIGHT, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    FLASHLIGHT_ACTION));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_SCREEN_RECORD, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    SCREEN_RECORD_ACTION));
                                }
                            }
                        });

                XposedHelpers.findAndHookMethod(DEFAULT_ACTION_VIEW_MODEL_FACTORY,
                        classLoader,
                        "createActionViewModel",
                        SecGlobalActions.class,
                        String.class,
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                setActionViewModelDefaults(param);
                                FirefdsKitActionViewModelsFactory.initFactory(mFlashlightObject,
                                        actionIcons,
                                        actionStrings,
                                        actionViewModelDefaults,
                                        prefUnlockKeyguardBeforeActionExecute);
                                FirefdsKitActionViewModel firefdsKitActionViewModel = FirefdsKitActionViewModelsFactory
                                        .getActionViewModel((String) param.args[1]);
                                if (firefdsKitActionViewModel != null) {
                                    param.setResult(firefdsKitActionViewModel);
                                }
                            }
                        });

                XposedHelpers.findAndHookMethod(GLOBAL_ACTION_ITEM_VIEW,
                        classLoader,
                        "setViewAttrs",
                        View.class,
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                ActionViewModel actionViewModel = (ActionViewModel) XposedHelpers.
                                        getObjectField(param.thisObject, "mViewModel");
                                ResourceFactory resourceFactory = (ResourceFactory) XposedHelpers
                                        .getObjectField(param.thisObject, "mResourceFactory");
                                ImageView localImageView = ((View) param.args[0])
                                        .findViewById(resourceFactory.getResourceID("sec_global_actions_icon"));
                                if (actionViewModel.getIcon() != null) {
                                    localImageView.setImageDrawable(actionViewModel.getIcon());
                                }
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static void setActionViewModelDefaults(XC_MethodHook.MethodHookParam param) {
        Map<String, Object> actionViewModelDefaults = new HashMap<>();

        UtilFactory mUtilFactory = (UtilFactory) XposedHelpers.getObjectField(param.thisObject, "mUtilFactory");
        KeyGuardManagerWrapper mKeyGuardManagerWrapper = (KeyGuardManagerWrapper) XposedHelpers.callMethod(mUtilFactory,
                "get",
                KeyGuardManagerWrapper.class);

        actionViewModelDefaults.put("mContext", XposedHelpers.getObjectField(mKeyGuardManagerWrapper, "mContext"));
        actionViewModelDefaults.put("mGlobalActions", mSecGlobalActionsPresenter);
        actionViewModelDefaults.put("mFeatureFactory", XposedHelpers.getObjectField(param.thisObject, "mFeatureFactory"));
        actionViewModelDefaults.put("mConditionChecker", XposedHelpers.getObjectField(param.thisObject, "mConditionChecker"));
        actionViewModelDefaults.put("mKeyGuardManagerWrapper", mKeyGuardManagerWrapper);

        XSysUIGlobalActions.actionViewModelDefaults = actionViewModelDefaults;
    }
}