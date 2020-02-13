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

import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.SecGlobalActionsPresenter;
import com.samsung.android.globalactions.presentation.view.ResourceFactory;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModelFactory;
import com.samsung.android.globalactions.presentation.viewmodel.ViewType;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.UtilFactory;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.actionViewModels.FirefdsKitActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.FlashLightActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.RestartActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.RestartSystemUiActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.ScreenRecordActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.ScreenShotActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.UserSwitchActionViewModel;
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

public class XSysUIGlobalActions {

    private static final String GLOBAL_ACTIONS_PACKAGE = "com.samsung.android.globalactions.presentation";
    private static final String SEC_GLOBAL_ACTIONS_PRESENTER = GLOBAL_ACTIONS_PACKAGE + ".SecGlobalActionsPresenter";
    private static final String DEFAULT_ACTION_VIEW_MODEL_FACTORY = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.DefaultActionViewModelFactory";
    private static final String SEC_GLOBAL_ACTIONS_DIALOG_BASE = GLOBAL_ACTIONS_PACKAGE + ".view.SecGlobalActionsDialogBase";
    private static final String GLOBAL_ACTION_ITEM_VIEW = GLOBAL_ACTIONS_PACKAGE + ".view.GlobalActionItemView";
    private static final String FLASHLIGHT_CONTROLLER_IMPL_CLASS = SYSTEM_UI + ".statusbar.policy.FlashlightControllerImpl";

    private static SecGlobalActionsPresenter mSecGlobalActionsPresenter;
    private static Object[] actionViewModelDefaults;
    private static String mRecoveryStr;
    private static String mDownloadStr;
    private static String mScreenshotStr;
    private static String mSwitchUserStr;
    private static String mRestartSystemUiStr;
    private static String mFlashlightStr;
    private static String mScreenRecordStr;
    private static Drawable mRecoveryIcon;
    private static Drawable mDownloadIcon;
    private static Drawable mScreenshotIcon;
    private static Drawable mSwitchUserIcon;
    private static Drawable mRestartSystemUiIcon;
    private static Drawable mFlashLightIcon;
    private static Drawable mScreenRecordIcon;
    private static String mRebootConfirmRecoveryStr;
    private static String mRebootConfirmDownloadStr;
    private static String mRestartSystemUiConfirmStr;
    private static Object mFlashlightObject;
    private static String mFlashlightOnStr;
    private static String mFlashlightOffStr;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        Class<?> flashlightControllerImplClass = XposedHelpers.findClass(FLASHLIGHT_CONTROLLER_IMPL_CLASS, classLoader);

        XposedHelpers.findAndHookConstructor(flashlightControllerImplClass,
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        mFlashlightObject = param.thisObject;
                    }
                });

        final Class<?> secGlobalActionsDialogBaseClass = XposedHelpers.findClass(SEC_GLOBAL_ACTIONS_DIALOG_BASE, classLoader);

        if (prefs.getBoolean(PREF_DISABLE_POWER_MENU_SECURE_LOCKSCREEN, false)) {
            XposedHelpers.findAndHookMethod(secGlobalActionsDialogBaseClass,
                    "showDialog",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                            if (mKeyguardManager.isKeyguardLocked()) {
                                param.setResult(null);
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

        if (prefs.getBoolean(PREF_ENABLE_ADVANCED_POWER_MENU, false)) {
            try {
                XposedBridge.hookAllConstructors(secGlobalActionsDialogBaseClass,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                                Context ctx = (Context) param.args[0];
                                Resources res = ctx.getResources();
                                Context gbContext = Utils.getGbContext(ctx, res.getConfiguration());

                                mRecoveryStr = prefs.getString(PREF_CUSTOM_RECOVERY,
                                        gbContext.getString(R.string.reboot_recovery));
                                mDownloadStr = gbContext.getString(R.string.reboot_download);
                                mScreenshotStr = gbContext.getString(R.string.screenshot);
                                mSwitchUserStr = gbContext.getString(R.string.switchUser);
                                mRestartSystemUiStr = gbContext.getString(R.string.restartUI);
                                mFlashlightStr = gbContext.getString(R.string.flashlight);
                                mScreenRecordStr = gbContext.getString(R.string.screen_record);

                                mRecoveryIcon = gbContext.getDrawable(R.drawable.tw_ic_do_recovery_stock);
                                mDownloadIcon = gbContext.getDrawable(R.drawable.tw_ic_do_download_stock);
                                mScreenshotIcon = gbContext.getDrawable(R.drawable.tw_ic_do_screenshot_stock);
                                mSwitchUserIcon = gbContext.getDrawable(R.drawable.tw_ic_do_users_stock);
                                mRestartSystemUiIcon = gbContext.getDrawable(R.drawable.tw_ic_do_restart_ui_stock);
                                mFlashLightIcon = gbContext.getDrawable(R.drawable.tw_ic_do_torchlight_stock);
                                mScreenRecordIcon = gbContext.getDrawable(R.drawable.tw_ic_do_screenrecord_stock);

                                mRebootConfirmRecoveryStr = prefs.getString(PREF_CUSTOM_RECOVERY_CONFIRMATION,
                                        gbContext.getString(R.string.reboot_confirm_recovery));
                                mRebootConfirmDownloadStr = gbContext
                                        .getString(R.string.reboot_confirm_download);
                                mRestartSystemUiConfirmStr = gbContext
                                        .getString(R.string.restartUI);

                                mFlashlightOnStr = gbContext
                                        .getString(R.string.flashlight_on);
                                mFlashlightOffStr = gbContext
                                        .getString(R.string.flashlight_off);

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
                                FirefdsKitActionViewModel firefdsKitActionViewModel = null;
                                String actionName = (String) param.args[1];
                                switch (actionName) {
                                    case (RECOVERY_ACTION):
                                        firefdsKitActionViewModel = setRestartActionViewModel(actionName,
                                                mRecoveryStr,
                                                mRebootConfirmRecoveryStr,
                                                RECOVERY_ACTION);
                                        break;
                                    case (DOWNLOAD_ACTION):
                                        firefdsKitActionViewModel = setRestartActionViewModel(actionName,
                                                mDownloadStr,
                                                mRebootConfirmDownloadStr,
                                                DOWNLOAD_ACTION);
                                        break;
                                    case (SCREENSHOT_ACTION):
                                        firefdsKitActionViewModel =
                                                setActionViewModel(ScreenShotActionViewModel.class,
                                                        actionName,
                                                        mScreenshotStr,
                                                        null);
                                        break;
                                    case (MULTIUSER_ACTION):
                                        firefdsKitActionViewModel =
                                                setActionViewModel(UserSwitchActionViewModel.class,
                                                        actionName,
                                                        mSwitchUserStr,
                                                        null);
                                        break;
                                    case (RESTART_UI_ACTION):
                                        firefdsKitActionViewModel =
                                                setActionViewModel(RestartSystemUiActionViewModel.class,
                                                        actionName,
                                                        mRestartSystemUiStr,
                                                        mRestartSystemUiConfirmStr);
                                        break;
                                    case (FLASHLIGHT_ACTION):
                                        firefdsKitActionViewModel =
                                                setFlashLightActionViewModel();
                                        break;
                                    case (SCREEN_RECORD_ACTION):
                                        firefdsKitActionViewModel =
                                                setActionViewModel(ScreenRecordActionViewModel.class,
                                                        actionName,
                                                        mScreenRecordStr,
                                                        null);
                                        break;
                                }
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
                                switch (actionViewModel.getActionInfo().getName()) {
                                    case RECOVERY_ACTION:
                                        localImageView.setImageDrawable(mRecoveryIcon);
                                        break;
                                    case DOWNLOAD_ACTION:
                                        localImageView.setImageDrawable(mDownloadIcon);
                                        break;
                                    case SCREENSHOT_ACTION:
                                        localImageView.setImageDrawable(mScreenshotIcon);
                                        break;
                                    case MULTIUSER_ACTION:
                                        localImageView.setImageDrawable(mSwitchUserIcon);
                                        break;
                                    case RESTART_UI_ACTION:
                                        localImageView.setImageDrawable(mRestartSystemUiIcon);
                                        break;
                                    case FLASHLIGHT_ACTION:
                                        localImageView.setImageDrawable(mFlashLightIcon);
                                        break;
                                    case SCREEN_RECORD_ACTION:
                                        localImageView.setImageDrawable(mScreenRecordIcon);
                                        break;
                                }
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static FirefdsKitActionViewModel setRestartActionViewModel(String actionName,
                                                                       String actionLabel,
                                                                       String actionDescription,
                                                                       String rebootAction) {

        RestartActionViewModel restartActionViewModel = (RestartActionViewModel) setActionViewModel(RestartActionViewModel.class,
                actionName,
                actionLabel,
                actionDescription);
        restartActionViewModel.setRebootOption(rebootAction);
        return restartActionViewModel;
    }

    private static FirefdsKitActionViewModel setFlashLightActionViewModel() {
        FlashLightActionViewModel.setFlashlightObject(mFlashlightObject);
        FlashLightActionViewModel.setFlashlightOnStr(mFlashlightOnStr);
        FlashLightActionViewModel.setFlashlightOffStr(mFlashlightOffStr);

        return setActionViewModel(FlashLightActionViewModel.class, FLASHLIGHT_ACTION, mFlashlightStr, null);
    }

    private static FirefdsKitActionViewModel setActionViewModel(Class<?> basicActionViewModelClass,
                                                                String actionName,
                                                                String actionLabel,
                                                                String actionDescription) {
        FirefdsKitActionViewModel firefdsKitActionViewModel = null;
        try {
            Constructor<?> constructor = basicActionViewModelClass.getConstructor(Object[].class);
            Object[] param = {actionViewModelDefaults};
            firefdsKitActionViewModel = (FirefdsKitActionViewModel) constructor.newInstance(param);
            ActionInfo actionInfo = setActionInfo(actionName, actionLabel, actionDescription);
            firefdsKitActionViewModel.setActionInfo(actionInfo);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        return firefdsKitActionViewModel;
    }

    private static void setActionViewModelDefaults(XC_MethodHook.MethodHookParam param) {
        Object[] actionViewModelDefaults = new Object[2];

        UtilFactory mUtilFactory = (UtilFactory) XposedHelpers.getObjectField(param.thisObject, "mUtilFactory");
        KeyGuardManagerWrapper mKeyGuardManagerWrapper = (KeyGuardManagerWrapper) XposedHelpers.callMethod(mUtilFactory,
                "get",
                KeyGuardManagerWrapper.class);

        actionViewModelDefaults[0] = XposedHelpers.getObjectField(mKeyGuardManagerWrapper, "mContext");
        actionViewModelDefaults[1] = mSecGlobalActionsPresenter;

        XSysUIGlobalActions.actionViewModelDefaults = actionViewModelDefaults;
    }

    private static ActionInfo setActionInfo(String actionName,
                                            String actionLabel,
                                            String actionDescription) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setName(actionName);
        actionInfo.setLabel(actionLabel);
        actionInfo.setDescription(actionDescription);
        actionInfo.setViewType(ViewType.CENTER_ICON_3P_VIEW);
        return actionInfo;
    }
}