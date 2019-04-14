package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;
import android.os.PowerManager;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.presentation.strategies.ActionInteractionStrategy;
import com.samsung.android.globalactions.presentation.strategies.SecureConfirmStrategy;
import com.samsung.android.globalactions.presentation.strategies.SoftwareUpdateStrategy;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.ResourcesWrapper;
import com.samsung.android.globalactions.util.SystemConditions;
import com.samsung.android.globalactions.util.ToastController;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RestartActionViewModel implements ActionViewModel {
    private final ConditionChecker mConditionChecker;
    private final FeatureFactory mFeatureFactory;
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private final KeyGuardManagerWrapper mKeyguardManagerWrapper;
    private final ResourcesWrapper mResourcesWrapper;
    private final ToastController mToastController;
    private Context mContext;
    private String rebootOption;

    public RestartActionViewModel(Map<String, Object> actionViewModelDefaults,
                                  String paramRebootOption) {
        mGlobalActions =
                (SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
        mKeyguardManagerWrapper =
                (KeyGuardManagerWrapper) actionViewModelDefaults.get("mKeyGuardManagerWrapper");
        mConditionChecker = (ConditionChecker) actionViewModelDefaults.get("mConditionChecker");
        mFeatureFactory = (FeatureFactory) actionViewModelDefaults.get("mFeatureFactory");
        mToastController = (ToastController) actionViewModelDefaults.get("ToastController");
        mResourcesWrapper = (ResourcesWrapper) actionViewModelDefaults.get("ResourcesWrapper");
        mContext = (Context) actionViewModelDefaults.get("mContext");
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

        for (ActionInteractionStrategy onPressRestartAction :
                mFeatureFactory.createActionInteractionStrategies(mInfo.getName())) {
            if (onPressRestartAction.onPressRestartAction()) {
                return;
            }
        }
        if (!mGlobalActions.isActionConfirming()) {
            mGlobalActions.confirmAction(this);
        } else if (mConditionChecker.isEnabled(SystemConditions.IS_FMM_LOCKED)) {
            this.mToastController.showToast(mResourcesWrapper.getString(17040348), 1);
        } else {
            List<SoftwareUpdateStrategy> createSoftwareUpdateStrategy =
                    mFeatureFactory.createSoftwareUpdateStrategy(mGlobalActions, "restart");
            boolean z = true;
            Iterator it = createSoftwareUpdateStrategy.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (!((SoftwareUpdateStrategy) it.next()).onUpdate()) {
                    z = false;
                    break;
                }
            }
            if (z) {
                for (SoftwareUpdateStrategy update : createSoftwareUpdateStrategy) {
                    update.update();
                }
                mGlobalActions.dismissDialog(false);
            } else if (isNeedSecureConfirm()) {
                for (SecureConfirmStrategy doActionBeforeSecureConfirm :
                        mFeatureFactory.createSecureConfirmStrategy(mInfo.getName())) {
                    doActionBeforeSecureConfirm.doActionBeforeSecureConfirm();
                }
                mGlobalActions.registerSecureConfirmAction(this);
                mKeyguardManagerWrapper.setPendingIntentAfterUnlock("shutdown");
                mGlobalActions.hideDialogOnSecureConfirm();
            } else {
                reboot();
            }
        }
    }

    public void onPressSecureConfirm() {
        reboot();
    }

    private void reboot() {
        ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE)).reboot(rebootOption);
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
