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

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.SecGlobalActionsPresenter;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
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
import sb.firefds.pie.firefdskit.actionViewModels.RestartActionViewModel;
import sb.firefds.pie.firefdskit.actionViewModels.ScreenShotActionViewModel;
import sb.firefds.pie.firefdskit.utils.Packages;

public class XSysUIGlobalActions {

    private static final int RECOVERY_RESTART_ACTION = 3;
    private static final int DOWNLOAD_RESTART_ACTION = 4;
    private static final String GLOBAL_ACTIONS_PACKAGE = "com.samsung.android.globalactions";
    private static final String SEC_GLOBAL_ACTIONS_PRESENTER =
            GLOBAL_ACTIONS_PACKAGE + ".presentation.SecGlobalActionsPresenter";
    private static final String DEFAULT_ACTION_VIEW_MODEL_FACTORY =
            GLOBAL_ACTIONS_PACKAGE + ".presentation.viewmodel.DefaultActionViewModelFactory";
    private static SecGlobalActionsPresenter mSecGlobalActionsPresenter;
    private static UtilFactory mUtilFactory;
    private static Map<String, Object> actionViewModelDefaults;
    private static XSharedPreferences prefs;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

        XSysUIGlobalActions.prefs = prefs;

        if (prefs.getBoolean("enableAdvancedPowerMenu", false)) {
            try {
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
                                mSecGlobalActionsPresenter
                                        .addAction(actionViewModelFactory.createActionViewModel(
                                                (SecGlobalActionsPresenter) param.thisObject,
                                                "recovery"));
                                mSecGlobalActionsPresenter
                                        .addAction(actionViewModelFactory.createActionViewModel(
                                                (SecGlobalActionsPresenter) param.thisObject,
                                                "download"));
                                if (prefs.getBoolean("enableDataMode", false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "data_mode"));
                                }
                                if (prefs.getBoolean("enableScreenshot", false)) {
                                    mSecGlobalActionsPresenter
                                            .addAction(actionViewModelFactory.createActionViewModel(
                                                    (SecGlobalActionsPresenter) param.thisObject,
                                                    "screenshot"));
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
                                                "Recovery",
                                                "Reboot to recovery mode",
                                                RECOVERY_RESTART_ACTION,
                                                getIdentifier(mUtilFactory,
                                                        "stat_notify_lockscreen_setting",
                                                        "drawable"));
                                        param.setResult(restartActionViewModel);
                                        break;
                                    case ("download"):
                                        restartActionViewModel = setRestartActionViewModel("download",
                                                "Download",
                                                "Reboot to download mode",
                                                DOWNLOAD_RESTART_ACTION,
                                                getIdentifier(mUtilFactory,
                                                        "stat_notify_safe_mode",
                                                        "drawable"));
                                        param.setResult(restartActionViewModel);
                                        break;
                                    case ("screenshot"):
                                        ScreenShotActionViewModel screenShotActionView =
                                                setScreenShotActionViewModel();
                                        param.setResult(screenShotActionView);
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
                                                                    int rebootAction,
                                                                    int actionIcon) {

        RestartActionViewModel restartActionViewModel =
                new RestartActionViewModel(actionViewModelDefaults, rebootAction);
        ActionInfo actionInfo = setActionInfo(actionName,
                actionLabel,
                actionDescription,
                actionIcon);
        XposedHelpers.callMethod(restartActionViewModel,
                "setActionInfo",
                actionInfo);
        return restartActionViewModel;
    }

    private static ScreenShotActionViewModel setScreenShotActionViewModel() {
        ScreenShotActionViewModel screenShotActionViewModel =
                new ScreenShotActionViewModel(actionViewModelDefaults);
        ActionInfo actionInfo = setActionInfo("screenshot",
                "Screenshot",
                null,
                getIdentifier(mUtilFactory,
                        "stat_notify_image",
                        "drawable"));
        screenShotActionViewModel.setActionInfo(actionInfo);
        return screenShotActionViewModel;
    }

    private static void setActionViewModelDefaults(XC_MethodHook.MethodHookParam param) {
        Map<String, Object> actionViewModelDefaults = new HashMap<>();

        mUtilFactory = (UtilFactory) XposedHelpers.getObjectField(param.thisObject, "mUtilFactory");
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
                                            String actionDescription,
                                            int actionIcon) {
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setName(actionName);
        actionInfo.setLabel(actionLabel);
        actionInfo.setDescription(actionDescription);
        actionInfo.setIcon(actionIcon);
        actionInfo.setViewType(ViewType.CENTER_ICON_3P_VIEW);
        return actionInfo;
    }

    private static int getIdentifier(UtilFactory utilFactory, String name, String defType) {
        return (utilFactory
                .get(Context.class))
                .getResources()
                .getIdentifier(name, defType, Packages.SYSTEM_UI);
    }
}