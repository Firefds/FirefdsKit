package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.presentation.strategies.SecureConfirmStrategy;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.SystemConditions;

import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getActionViewModelDefaults;
import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.isUnlockKeyguardBeforeActionExecute;
import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_ACTION;
import static sb.firefds.pie.firefdskit.utils.Packages.FIREFDSKIT;

public abstract class RestartActionViewModel extends FirefdsKitActionViewModel {
    private static final String REBOOT_ACTIVITY = FIREFDSKIT + ".activities.WanamRebootActivity";
    private String rebootOption;
    private FeatureFactory mFeatureFactory;
    private ConditionChecker mConditionChecker;
    private KeyGuardManagerWrapper mKeyGuardManagerWrapper;

    RestartActionViewModel() {

        super();
        mFeatureFactory = getActionViewModelDefaults().getFeatureFactory();
        mConditionChecker = getActionViewModelDefaults().getConditionChecker();
        mKeyGuardManagerWrapper = getActionViewModelDefaults().getKeyGuardManagerWrapper();
    }

    @Override
    public void onPress() {

        if (!getGlobalActions().isActionConfirming()) {
            getGlobalActions().confirmAction(this);
        } else {
            if (isUnlockKeyguardBeforeActionExecute()) {
                if (mConditionChecker.isEnabled(SystemConditions.IS_SECURE_KEYGUARD)) {
                    for (SecureConfirmStrategy strategy3 : mFeatureFactory.createSecureConfirmStrategy(getActionInfo().getName())) {
                        strategy3.doActionBeforeSecureConfirm();
                    }
                    getGlobalActions().registerSecureConfirmAction(this);
                    mKeyGuardManagerWrapper.setPendingIntentAfterUnlock("shutdown");
                    getGlobalActions().hideDialogOnSecureConfirm();
                } else {
                    reboot();
                }
            } else {
                reboot();
            }
        }
    }

    @Override
    public void onPressSecureConfirm() {
        reboot();
    }

    private void reboot() {
        try {
            ((PowerManager) getContext().getSystemService(Context.POWER_SERVICE)).reboot(rebootOption);
        } catch (SecurityException e) {
            Intent rebootIntent = new Intent().setComponent(new ComponentName(FIREFDSKIT, REBOOT_ACTIVITY));
            Bundle b = new Bundle();
            b.putString(REBOOT_ACTION, rebootOption);
            rebootIntent.putExtras(b);
            rebootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(rebootIntent);
        }
    }

    void setRebootOption(String rebootOption) {
        this.rebootOption = rebootOption;
    }
}
