package sb.firefds.pie.firefdskit.actionViewModels;

import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getCustomRecovery;
import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getCustomRecoveryConfirmation;
import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.pie.firefdskit.utils.Constants.RECOVERY_ACTION;

class RestartRecoveryActionViewModel extends RestartActionViewModel {

    RestartRecoveryActionViewModel() {

        super();
        getActionInfo().setName(RECOVERY_ACTION);
        getActionInfo().setLabel(getCustomRecovery() == null ? getResources().getString(R.string.reboot_recovery)
                : getCustomRecovery());
        getActionInfo().setDescription(getCustomRecoveryConfirmation() == null ? getResources().getString(R.string.reboot_confirm_recovery)
                : getCustomRecoveryConfirmation());
        setDrawableIcon(getResources().getDrawable(R.drawable.tw_ic_do_recovery_stock, null));
        setRebootOption(RECOVERY_ACTION);
    }
}
