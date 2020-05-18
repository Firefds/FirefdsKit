/*
 * Copyright (C) 2020 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.q.firefdskit;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.SecGlobalActionsPresenter;
import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.presentation.view.ResourceFactory;
import com.samsung.android.globalactions.presentation.view.ResourceType;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModelFactory;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.SystemConditions;
import com.samsung.android.globalactions.util.UtilFactory;
import de.robv.android.xposed.*;
import sb.firefds.q.firefdskit.actionViewModels.ActionViewModelDefaults;
import sb.firefds.q.firefdskit.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Optional;

import static sb.firefds.q.firefdskit.actionViewModels.FirefdsKitActionViewModelsFactory.getActionViewModel;
import static sb.firefds.q.firefdskit.utils.Constants.*;
import static sb.firefds.q.firefdskit.utils.Packages.SYSTEM_UI;
import static sb.firefds.q.firefdskit.utils.Preferences.*;

public class XSysUIGlobalActions {

    private static final String GLOBAL_ACTIONS_PACKAGE = "com.samsung.android.globalactions.presentation";
    private static final String SEC_GLOBAL_ACTIONS_PRESENTER = GLOBAL_ACTIONS_PACKAGE + ".SecGlobalActionsPresenter";
    private static final String DEFAULT_ACTION_VIEW_MODEL_FACTORY = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.DefaultActionViewModelFactory";
    private static final String SEC_GLOBAL_ACTIONS_DIALOG_BASE = GLOBAL_ACTIONS_PACKAGE + ".view.SecGlobalActionsDialogBase";
    private static final String GLOBAL_ACTION_CONTENT_ITEM_VIEW = GLOBAL_ACTIONS_PACKAGE + ".view.GlobalActionsContentItemView";
    private static final String FLASHLIGHT_CONTROLLER_IMPL_CLASS = SYSTEM_UI + ".statusbar.policy.FlashlightControllerImpl";
    private static final String RESTART_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.RestartActionViewModel";
    private static final String SAFE_MODE_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.SafeModeActionViewModel";
    private static final String POWER_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.PowerActionViewModel";
    private static final String EMERGENCY_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.EmergencyActionViewModel";
    private static final String SIDE_KEY_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.SideKeyActionViewModel";
    private static final String DATA_MODE_ACTION_VIEW_MODEL = GLOBAL_ACTIONS_PACKAGE + ".viewmodel.DataModeActionViewModel";

    private static SecGlobalActionsPresenter mSecGlobalActionsPresenter;
    private static ActionViewModelDefaults actionViewModelDefaults;
    private static Object mFlashlightObject;
    private static boolean prefUnlockKeyguardBeforeActionExecute;
    private static boolean prefReplaceRecoveryIcon;
    private static String prefCustomRecovery;
    private static String prefCustomRecoveryConfirmation;
    private static Resources resources;

    public static void doHook(XSharedPreferences prefs, ClassLoader classLoader) {

        prefUnlockKeyguardBeforeActionExecute = prefs.getBoolean(PREF_UNLOCK_KEYGUARD_BEFORE_ACTION_EXECUTE, false);
        prefReplaceRecoveryIcon = prefs.getBoolean(PREF_REPLACE_RECOVERY_ICON, false);
        prefCustomRecovery = prefs.getString(PREF_CUSTOM_RECOVERY, null);
        prefCustomRecoveryConfirmation = prefs.getString(PREF_CUSTOM_RECOVERY_CONFIRMATION, null);

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

            XposedHelpers.findAndHookMethod(SIDE_KEY_ACTION_VIEW_MODEL,
                    classLoader,
                    "isNeedSecureConfirm",
                    isNeedSecureConfirmHook);

            XposedHelpers.findAndHookMethod(DATA_MODE_ACTION_VIEW_MODEL,
                    classLoader,
                    "isNeedSecureConfirm",
                    isNeedSecureConfirmHook);
        }

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
                                resources = gbContext.getResources();
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
                                    mSecGlobalActionsPresenter.clearActions(RESTART_ACTION);
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
                                getActionViewModel((String) param.args[1]).ifPresent(param::setResult);
                            }
                        });

                XposedHelpers.findAndHookMethod(GLOBAL_ACTION_CONTENT_ITEM_VIEW,
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
                                        .findViewById(resourceFactory.get(ResourceType.ID_ICON));
                                Optional.ofNullable(actionViewModel.getIcon())
                                        .ifPresent(localImageView::setImageDrawable);
                            }
                        });

            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }
    }

    public static Resources getResources() {
        return resources;
    }

    public static ActionViewModelDefaults getActionViewModelDefaults() {
        return actionViewModelDefaults;
    }

    public static Object getFlashlightObject() {
        return mFlashlightObject;
    }

    public static boolean isUnlockKeyguardBeforeActionExecute() {
        return prefUnlockKeyguardBeforeActionExecute;
    }

    public static boolean isReplaceRecoveryIcon() {
        return prefReplaceRecoveryIcon;
    }

    public static String getCustomRecovery() {
        return prefCustomRecovery;
    }

    public static String getCustomRecoveryConfirmation() {
        return prefCustomRecoveryConfirmation;
    }

    private static void setActionViewModelDefaults(XC_MethodHook.MethodHookParam param) {
        UtilFactory mUtilFactory = (UtilFactory) XposedHelpers.getObjectField(param.thisObject, "mUtilFactory");
        KeyGuardManagerWrapper mKeyGuardManagerWrapper = (KeyGuardManagerWrapper) XposedHelpers.callMethod(mUtilFactory,
                "get",
                KeyGuardManagerWrapper.class);
        WeakReference<Context> contextWeakReference =
                new WeakReference<>((Context) XposedHelpers.getObjectField(mKeyGuardManagerWrapper, "mContext"));
        actionViewModelDefaults = new ActionViewModelDefaults(contextWeakReference,
                mSecGlobalActionsPresenter,
                (FeatureFactory) XposedHelpers.getObjectField(param.thisObject, "mFeatureFactory"),
                (ConditionChecker) XposedHelpers.getObjectField(param.thisObject, "mConditionChecker"),
                mKeyGuardManagerWrapper);
    }

}