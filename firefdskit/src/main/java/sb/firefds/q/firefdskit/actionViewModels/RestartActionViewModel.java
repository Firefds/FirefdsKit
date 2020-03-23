package sb.firefds.q.firefdskit.actionViewModels;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.Keep;

import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.presentation.strategies.SecureConfirmStrategy;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;
import com.samsung.android.globalactions.util.SystemConditions;

import java.util.HashMap;

import static sb.firefds.q.firefdskit.utils.Constants.REBOOT_ACTION;
import static sb.firefds.q.firefdskit.utils.Packages.FIREFDSKIT;

@Keep
public class RestartActionViewModel extends FirefdsKitActionViewModel {
    private static final String REBOOT_ACTIVITY = FIREFDSKIT + ".activities.WanamRebootActivity";
    private String rebootOption;
    private FeatureFactory mFeatureFactory;
    private ConditionChecker mConditionChecker;
    private KeyGuardManagerWrapper mKeyGuardManagerWrapper;
    private boolean unlockKeyguardBeforeActionExecute;

    public RestartActionViewModel(HashMap<String, Object> actionViewModelDefaults) {
        super(actionViewModelDefaults);
        mFeatureFactory = (FeatureFactory) actionViewModelDefaults.get("mFeatureFactory");
        mConditionChecker = (ConditionChecker) actionViewModelDefaults.get("mConditionChecker");
        mKeyGuardManagerWrapper = (KeyGuardManagerWrapper) actionViewModelDefaults.get("mKeyGuardManagerWrapper");
    }

    @Override
    public void onPress() {

        if (!getmGlobalActions().isActionConfirming()) {
            getmGlobalActions().confirmAction(this);
        } else {
            if (unlockKeyguardBeforeActionExecute) {
                if (mConditionChecker.isEnabled(SystemConditions.IS_SECURE_KEYGUARD)) {
                    for (SecureConfirmStrategy strategy3 : mFeatureFactory.createSecureConfirmStrategy(getmGlobalActions(), getActionInfo().getName())) {
                        strategy3.doActionBeforeSecureConfirm(this, getmGlobalActions());
                    }
                    getmGlobalActions().registerSecureConfirmAction(this);
                    mKeyGuardManagerWrapper.setPendingIntentAfterUnlock("reboot");
                    getmGlobalActions().hideDialogOnSecureConfirm();
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
            ((PowerManager) getmContext().getSystemService(Context.POWER_SERVICE)).reboot(rebootOption);
        } catch (SecurityException e) {
            Intent rebootIntent = new Intent().setComponent(new ComponentName(FIREFDSKIT, REBOOT_ACTIVITY));
            Bundle b = new Bundle();
            b.putString(REBOOT_ACTION, rebootOption);
            rebootIntent.putExtras(b);
            rebootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getmContext().startActivity(rebootIntent);
        }
    }

    public void setRebootOption(String rebootOption) {
        this.rebootOption = rebootOption;
    }

    public void setUnlockKeyguardBeforeActionExecute(boolean unlockKeyguardBeforeActionExecute) {
        this.unlockKeyguardBeforeActionExecute = unlockKeyguardBeforeActionExecute;
    }
}
