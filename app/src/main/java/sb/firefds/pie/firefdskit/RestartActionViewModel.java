package sb.firefds.pie.firefdskit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.presentation.strategies.ActionInteractionStrategy;
import com.samsung.android.globalactions.presentation.strategies.SecureConfirmStrategy;
import com.samsung.android.globalactions.presentation.strategies.SoftwareUpdateStrategy;
import com.samsung.android.globalactions.presentation.strategies.WindowManagerFunctionStrategy;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.ResourcesWrapper;
import com.samsung.android.globalactions.util.SystemConditions;
import com.samsung.android.globalactions.util.ToastController;

import java.util.Iterator;
import java.util.List;

public class RestartActionViewModel implements ActionViewModel {
    private final String FIREFDS_PACKAGE = "sb.firefds.pie.firefdskit";
    private final String REBOOT_ACTIVITY = FIREFDS_PACKAGE + ".activities.WanamRebootActivity";
    private final ConditionChecker mConditionChecker;
    private final FeatureFactory mFeatureFactory;
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private final KeyGuardManagerWrapper mKeyguardManagerWrapper;
    private final ResourcesWrapper mResourcesWrapper;
    private final ToastController mToastController;
    private Context mContext;
    private int rebootOption;

    public RestartActionViewModel(Context paramContext,
                                  SecGlobalActions paramSecGlobalActions,
                                  ConditionChecker paramConditionChecker,
                                  FeatureFactory paramFeatureFactory,
                                  ToastController paramToastController,
                                  KeyGuardManagerWrapper paramKeyGuardManagerWrapper,
                                  ResourcesWrapper paramResourcesWrapper,
                                  int paramRebootOption) {
        mGlobalActions = paramSecGlobalActions;
        mConditionChecker = paramConditionChecker;
        mFeatureFactory = paramFeatureFactory;
        mToastController = paramToastController;
        mKeyguardManagerWrapper = paramKeyGuardManagerWrapper;
        mResourcesWrapper = paramResourcesWrapper;
        mContext = paramContext;
        rebootOption = paramRebootOption;
    }

    private boolean isNeedSecureConfirm() {
        boolean var1;
        var1 = !mConditionChecker.isEnabled(SystemConditions.IS_RMM_LOCKED)
                && !mConditionChecker.isEnabled(SystemConditions.IS_SIM_LOCK)
                && mConditionChecker.isEnabled(SystemConditions.IS_SECURE_KEYGUARD)
                && mConditionChecker.isEnabled(SystemConditions.IS_LOCK_NETWORK_AND_SECURITY)
                && mConditionChecker.isEnabled(SystemConditions.IS_ENCRYPTION_STATUS_ACTIVE);

        return var1;
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {

        for (ActionInteractionStrategy o : mFeatureFactory
                .createActionInteractionStrategies(mInfo.getName())) {
            if ((o).onPressRestartAction()) {
                return;
            }
        }

        if (!mGlobalActions.isActionConfirming()) {
            mGlobalActions.confirmAction(this);
        } else if (mConditionChecker.isEnabled(SystemConditions.IS_FMM_LOCKED)) {
            mToastController.showToast(mResourcesWrapper.getString(17040348), 1);
        } else {
            List var2 = mFeatureFactory.createSoftwareUpdateStrategy(mGlobalActions, "restart");
            boolean var3 = true;
            Iterator var1 = var2.iterator();

            boolean var4;
            while (true) {
                var4 = var3;
                if (!var1.hasNext()) {
                    break;
                }

                if (!((SoftwareUpdateStrategy) var1.next()).onUpdate()) {
                    var4 = false;
                    break;
                }
            }

            if (var4) {
                var1 = var2.iterator();

                while (var1.hasNext()) {
                    ((SoftwareUpdateStrategy) var1.next()).update();
                }

                mGlobalActions.dismissDialog(false);
            } else if (!isNeedSecureConfirm()) {
                reboot();
            } else {
                var1 = mFeatureFactory.createSecureConfirmStrategy(mInfo.getName()).iterator();

                while (var1.hasNext()) {
                    ((SecureConfirmStrategy) var1.next()).doActionBeforeSecureConfirm();
                }

                mGlobalActions.registerSecureConfirmAction(this);
                mKeyguardManagerWrapper.setPendingIntentAfterUnlock("shutdown");
                mGlobalActions.hideDialogOnSecureConfirm();
            }
        }
    }

    public void onPressSecureConfirm() {
        reboot();
    }

    void reboot() {

        for (WindowManagerFunctionStrategy o : mFeatureFactory
                .createWindowManagerFunctionStrategy(mGlobalActions, "REBOOT")) {
            (o).onReboot();
        }

        Intent rebootIntent = new Intent()
                .setComponent(new ComponentName(FIREFDS_PACKAGE, REBOOT_ACTIVITY));
        Bundle b = new Bundle();
        b.putInt("reboot", rebootOption);
        rebootIntent.putExtras(b);
        rebootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(rebootIntent);
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
