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
import com.samsung.android.globalactions.util.ResourcesWrapper;
import com.samsung.android.globalactions.util.ToastController;
import com.samsung.android.globalactions.util.UtilFactory;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.pie.firefdskit.actionViewModels.FlashLightActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.RestartActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.RestartSystemUiActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.ScreenShotActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.UserSwitchActionViewModel;
import sb.firefds.pie.firefdskit.utils.Packages;
import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Preferences.*;

public class XSysUIGlobalActions {

    private static final String RECOVERY_RESTART_ACTION = "recovery";
    private static final String DOWNLOAD_RESTART_ACTION = "download";
    private static final String GLOBAL_ACTIONS_PACKAGE =
            "com.samsung.android.globalactions.presentation";
    private static final String SEC_GLOBAL_ACTIONS_PRESENTER =
            GLOBAL_ACTIONS_PACKAGE + ".SecGlobalActionsPresenter";
    private static final String DEFAULT_ACTION_VIEW_MODEL_FACTORY =
            GLOBAL_ACTIONS_PACKAGE + ".viewmodel.DefaultActionViewModelFactory";
    private static final String SEC_GLOBAL_ACTIONS_DIALOG_BASE =
            GLOBAL_ACTIONS_PACKAGE + ".view.SecGlobalActionsDialogBase";
    private static final String GLOBAL_ACTION_ITEM_VIEW =
            GLOBAL_ACTIONS_PACKAGE + ".view.GlobalActionItemView";
    private static final String FLASHLIGHT_CONTROLLER_IMPL_CLASS =
            Packages.SYSTEM_UI + ".statusbar.policy.FlashlightControllerImpl";
    private static SecGlobalActionsPresenter mSecGlobalActionsPresenter;
    private static Map<String, Object> actionViewModelDefaults;
    private static String mRecoveryStr;
    private static String mDownloadStr;
    private static String mScreenshotStr;
    private static String mSwitchUserStr;
    private static String mRestartSystemUiStr;
    private static String mFlashlightStr;
    private static Drawable mRecoveryIcon;
    private static Drawable mDownloadIcon;
    private static Drawable mScreenshotIcon;
    private static Drawable mSwitchUserIcon;
    private static Drawable mRestartSystemUiIcon;
    private static Drawable mFlashLightIcon;
    private static String mRebootConfirmRecoveryStr;
    private static String mRebootConfirmDownloadStr;
    private static Object mFlashlightObject;
    private static String mFlaslightOnStr;
    private static String mFlaslightOffStr;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        Class<?> flashlightControllerImplClass =
                XposedHelpers.findClass(FLASHLIGHT_CONTROLLER_IMPL_CLASS, classLoader);

        XposedHelpers.findAndHookConstructor(flashlightControllerImplClass,
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        mFlashlightObject = param.thisObject;
                    }
                });

        final Class<?> secGlobalActionsDialogBaseClass =
                XposedHelpers.findClass(SEC_GLOBAL_ACTIONS_DIALOG_BASE, classLoader);

        if (prefs.getBoolean(PREF_ENABLE_ADVANCED_POWER_MENU, false)) {
            try {
                XposedBridge.hookAllConstructors(secGlobalActionsDialogBaseClass,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                                Context ctx = (Context) param.args[0];
                                Resources res = ctx.getResources();
                                Context gbContext = Utils.getGbContext(ctx, res.getConfiguration());

                                mRecoveryStr = gbContext.getString(R.string.reboot_recovery);
                                mDownloadStr = gbContext.getString(R.string.reboot_download);
                                mScreenshotStr = gbContext.getString(R.string.screenshot);
                                mSwitchUserStr = gbContext.getString(R.string.switchUser);
                                mRestartSystemUiStr = gbContext.getString(R.string.restartUI);
                                mFlashlightStr = gbContext.getString(R.string.flashlight);

                                mRecoveryIcon = gbContext
                                        .getDrawable(R.drawable.tw_ic_do_recovery_stock);
                                mDownloadIcon = gbContext
                                        .getDrawable(R.drawable.tw_ic_do_download_stock);
                                mScreenshotIcon = gbContext
                                        .getDrawable(R.drawable.tw_ic_do_screenshot_stock);
                                mSwitchUserIcon = gbContext
                                        .getDrawable(R.drawable.tw_ic_do_users_stock);
                                mRestartSystemUiIcon = gbContext
                                        .getDrawable(R.drawable.tw_ic_do_restart_ui_stock);
                                mFlashLightIcon = gbContext
                                        .getDrawable(R.drawable.tw_ic_do_torchlight_stock);

                                mRebootConfirmRecoveryStr = gbContext
                                        .getString(R.string.reboot_confirm_recovery);
                                mRebootConfirmDownloadStr = gbContext
                                        .getString(R.string.reboot_confirm_download);

                                mFlaslightOnStr = gbContext
                                        .getString(R.string.flashlight_on);
                                mFlaslightOffStr = gbContext
                                        .getString(R.string.flashlight_off);

                            }
                        });

                XposedHelpers.findAndHookMethod(SEC_GLOBAL_ACTIONS_PRESENTER,
                        classLoader,
                        "createDefaultActions",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                ActionViewModelFactory actionViewModelFactory =
                                        (ActionViewModelFactory) XposedHelpers
                                                .getObjectField(param.thisObject,
                                                        "mViewModelFactory");
                                mSecGlobalActionsPresenter =
                                        (SecGlobalActionsPresenter) param.thisObject;
                                if (!prefs.getBoolean(PREF_ENABLE_POWER_OFF, true)) {
                                    mSecGlobalActionsPresenter.clearActions("power");
                                }
                                if (!prefs.getBoolean(PREF_ENABLE_RESTART, true)) {
                                    mSecGlobalActionsPresenter.clearActions("restart");
                                }
                                if (!prefs.getBoolean(PREF_ENABLE_EMERGENCY_MODE, true)) {
                                    mSecGlobalActionsPresenter.clearActions("emergency");
                                }
                                if (prefs.getBoolean(PREF_ENABLE_RECOVERY, true)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "recovery"));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_DOWNLOAD, true)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "download"));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_DATA_MODE, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "data_mode"));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_SCREENSHOT, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "screenshot"));
                                }
                                if (prefs.getBoolean(PREF_SUPPORTS_MULTIPLE_USERS, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "multiuser"));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_RESTART_SYSTEMUI, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "restart_ui"));
                                }
                                if (prefs.getBoolean(PREF_ENABLE_FLASHLIGHT, false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "flashlight"));
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
                                RestartActionViewModel restartActionViewModel;
                                switch ((String) param.args[1]) {
                                    case ("recovery"):
                                        restartActionViewModel = setRestartActionViewModel("recovery",
                                                mRecoveryStr,
                                                mRebootConfirmRecoveryStr,
                                                RECOVERY_RESTART_ACTION);
                                        param.setResult(restartActionViewModel);
                                        break;
                                    case ("download"):
                                        restartActionViewModel = setRestartActionViewModel("download",
                                                mDownloadStr,
                                                mRebootConfirmDownloadStr,
                                                DOWNLOAD_RESTART_ACTION);
                                        param.setResult(restartActionViewModel);
                                        break;
                                    case ("screenshot"):
                                        ScreenShotActionViewModel screenShotActionView =
                                                setScreenShotActionViewModel();
                                        param.setResult(screenShotActionView);
                                        break;
                                    case ("multiuser"):
                                        UserSwitchActionViewModel userSwitchActionViewModel =
                                                setUserSwitchActionViewMidel();
                                        param.setResult(userSwitchActionViewModel);
                                        break;
                                    case ("restart_ui"):
                                        RestartSystemUiActionViewModel restartSystemUiActionViewModel =
                                                setRestartSystemUiActionViewModel();
                                        param.setResult(restartSystemUiActionViewModel);
                                        break;
                                    case ("flashlight"):
                                        FlashLightActionViewModel flashLightActionViewModel =
                                                setFlashLightActionViewModel();
                                        param.setResult(flashLightActionViewModel);
                                        break;
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
                                        .findViewById(resourceFactory
                                                .getResourceID("sec_global_actions_icon"));
                                switch (actionViewModel.getActionInfo().getName()) {
                                    case "recovery":
                                        localImageView.setImageDrawable(mRecoveryIcon);
                                        break;
                                    case "download":
                                        localImageView.setImageDrawable(mDownloadIcon);
                                        break;
                                    case "screenshot":
                                        localImageView.setImageDrawable(mScreenshotIcon);
                                        break;
                                    case "multiuser":
                                        localImageView.setImageDrawable(mSwitchUserIcon);
                                        break;
                                    case "restart_ui":
                                        localImageView.setImageDrawable(mRestartSystemUiIcon);
                                        break;
                                    case "flashlight":
                                        localImageView.setImageDrawable(mFlashLightIcon);
                                        break;
                                }
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static RestartActionViewModel setRestartActionViewModel(String actionName,
                                                                    String actionLabel,
                                                                    String actionDescription,
                                                                    String rebootAction) {

        RestartActionViewModel restartActionViewModel =
                new RestartActionViewModel(actionViewModelDefaults, rebootAction);
        ActionInfo actionInfo = setActionInfo(actionName,
                actionLabel,
                actionDescription);
        XposedHelpers.callMethod(restartActionViewModel,
                "setActionInfo",
                actionInfo);
        return restartActionViewModel;
    }

    private static ScreenShotActionViewModel setScreenShotActionViewModel() {
        ScreenShotActionViewModel screenShotActionViewModel =
                new ScreenShotActionViewModel(actionViewModelDefaults);
        ActionInfo actionInfo = setActionInfo("screenshot",
                mScreenshotStr,
                null);
        screenShotActionViewModel.setActionInfo(actionInfo);
        return screenShotActionViewModel;
    }

    private static UserSwitchActionViewModel setUserSwitchActionViewMidel() {
        UserSwitchActionViewModel userSwitchActionViewModel =
                new UserSwitchActionViewModel(actionViewModelDefaults);
        ActionInfo actionInfo = setActionInfo("multiuser",
                mSwitchUserStr,
                null);
        userSwitchActionViewModel.setActionInfo(actionInfo);
        return userSwitchActionViewModel;
    }

    private static RestartSystemUiActionViewModel setRestartSystemUiActionViewModel() {
        RestartSystemUiActionViewModel restartSystemUiActionViewModel =
                new RestartSystemUiActionViewModel(actionViewModelDefaults);
        ActionInfo actionInfo = setActionInfo("restart_ui",
                mRestartSystemUiStr,
                null);
        restartSystemUiActionViewModel.setActionInfo(actionInfo);
        return restartSystemUiActionViewModel;
    }

    private static FlashLightActionViewModel setFlashLightActionViewModel() {
        FlashLightActionViewModel flashLightActionViewModel =
                new FlashLightActionViewModel(actionViewModelDefaults,
                        mFlashlightObject,
                        mFlaslightOnStr,
                        mFlaslightOffStr);
        ActionInfo actionInfo = setActionInfo("flashlight",
                mFlashlightStr,
                null);
        flashLightActionViewModel.setActionInfo(actionInfo);
        return flashLightActionViewModel;
    }

    private static void setActionViewModelDefaults(XC_MethodHook.MethodHookParam param) {
        Map<String, Object> actionViewModelDefaults = new HashMap<>();

        UtilFactory mUtilFactory = (UtilFactory) XposedHelpers.getObjectField(param.thisObject, "mUtilFactory");
        KeyGuardManagerWrapper mKeyGuardManagerWrapper =
                (KeyGuardManagerWrapper) XposedHelpers.callMethod(mUtilFactory,
                        "get",
                        KeyGuardManagerWrapper.class);

        actionViewModelDefaults.put("mContext",
                XposedHelpers.getObjectField(mKeyGuardManagerWrapper, "mContext"));
        actionViewModelDefaults.put("mSecGlobalActionsPresenter",
                mSecGlobalActionsPresenter);
        actionViewModelDefaults.put("mConditionChecker",
                XposedHelpers.getObjectField(param.thisObject, "mConditionChecker"));
        actionViewModelDefaults.put("mFeatureFactory",
                XposedHelpers.getObjectField(param.thisObject, "mFeatureFactory"));
        actionViewModelDefaults.put("ToastController",
                XposedHelpers.callMethod(mUtilFactory, "get", ToastController.class));
        actionViewModelDefaults.put("mKeyGuardManagerWrapper",
                mKeyGuardManagerWrapper);
        actionViewModelDefaults.put("ResourcesWrapper",
                XposedHelpers.callMethod(mUtilFactory, "get", ResourcesWrapper.class));
        actionViewModelDefaults.put("mUtilFactory",
                mUtilFactory);

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