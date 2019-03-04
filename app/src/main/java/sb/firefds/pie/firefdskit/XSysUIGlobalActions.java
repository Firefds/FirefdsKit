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
import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModelFactory;
import com.samsung.android.globalactions.presentation.viewmodel.ViewType;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.ResourcesWrapper;
import com.samsung.android.globalactions.util.ToastController;
import com.samsung.android.globalactions.util.UtilFactory;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class XSysUIGlobalActions {

    private static final int RECOVERY_RESTART_ACTION = 3;
    private static final int DOWNLOAD_RESTART_ACTION = 4;
    private static final String GLOBAL_ACTIONS_PACKAGE = "com.samsung.android.globalactions";
    private static final String SEC_GLOBAL_ACTIONS_PRESENTER =
            GLOBAL_ACTIONS_PACKAGE + ".presentation.SecGlobalActionsPresenter";
    private static final String DEFAULT_ACTION_VIEW_MODEL_FACTORY =
            GLOBAL_ACTIONS_PACKAGE + ".presentation.viewmodel.DefaultActionViewModelFactory";
    private static SecGlobalActionsPresenter mSecGlobalActionsPresenter;

    public static void doHook(final XSharedPreferences prefs, final ClassLoader classLoader) {

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
                                                "data_mode"));
                                mSecGlobalActionsPresenter
                                        .addAction(actionViewModelFactory.createActionViewModel(
                                                (SecGlobalActionsPresenter) param.thisObject,
                                                "recovery"));
                                mSecGlobalActionsPresenter
                                        .addAction(actionViewModelFactory.createActionViewModel(
                                                (SecGlobalActionsPresenter) param.thisObject,
                                                "download"));
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
                                RestartActionViewModel restartActionViewModel;
                                switch ((String) param.args[1]) {
                                    case ("recovery"):
                                        restartActionViewModel = setRestartActionViewModel(param,
                                                "recovery",
                                                "Recovery",
                                                "Reboot to recovery mode",
                                                RECOVERY_RESTART_ACTION,
                                                android.R.drawable.ic_popup_sync,
                                                ViewType.CENTER_ICON_3P_VIEW);
                                        param.setResult(restartActionViewModel);
                                        break;
                                    case ("download"):
                                        restartActionViewModel = setRestartActionViewModel(param,
                                                "download",
                                                "Download",
                                                "Reboot to download mode",
                                                DOWNLOAD_RESTART_ACTION,
                                                android.R.drawable.ic_dialog_alert,
                                                ViewType.CENTER_ICON_3P_VIEW);
                                        param.setResult(restartActionViewModel);
                                        break;
                                }
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    private static RestartActionViewModel setRestartActionViewModel(XC_MethodHook.MethodHookParam param,
                                                                    String actionName,
                                                                    String actionLabel,
                                                                    String actionDescription,
                                                                    int rebootAction,
                                                                    int actionIcon,
                                                                    ViewType actionViewType) {
        UtilFactory mUtilFactory =
                (UtilFactory) XposedHelpers.getObjectField(param.thisObject, "mUtilFactory");

        KeyGuardManagerWrapper mKeyGuardManagerWrapper =
                (KeyGuardManagerWrapper) XposedHelpers.callMethod(mUtilFactory,
                        "get",
                        KeyGuardManagerWrapper.class);

        RestartActionViewModel restartActionViewModel = new RestartActionViewModel(
                (Context) XposedHelpers.getObjectField(mKeyGuardManagerWrapper, "mContext"),
                mSecGlobalActionsPresenter,
                (ConditionChecker) XposedHelpers.getObjectField(param.thisObject,
                        "mConditionChecker"),
                (FeatureFactory) XposedHelpers.getObjectField(param.thisObject,
                        "mFeatureFactory"),
                (ToastController) XposedHelpers.callMethod(mUtilFactory,
                        "get",
                        ToastController.class),
                mKeyGuardManagerWrapper,
                (ResourcesWrapper) XposedHelpers.callMethod(mUtilFactory,
                        "get",
                        ResourcesWrapper.class),
                rebootAction);
        ActionInfo actionInfo = new ActionInfo();
        actionInfo.setName(actionName);
        actionInfo.setLabel(actionLabel);
        actionInfo.setDescription(actionDescription);
        actionInfo.setIcon(actionIcon);
        actionInfo.setViewType(actionViewType);
        XposedHelpers.callMethod(restartActionViewModel,
                "setActionInfo",
                actionInfo);
        return restartActionViewModel;
    }
}