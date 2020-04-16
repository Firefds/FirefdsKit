package sb.firefds.q.firefdskit.actionViewModels;

import sb.firefds.q.firefdskit.R;

import static sb.firefds.q.firefdskit.XSysUIGlobalActions.getCustomRecovery;
import static sb.firefds.q.firefdskit.XSysUIGlobalActions.getCustomRecoveryConfirmation;
import static sb.firefds.q.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.q.firefdskit.XSysUIGlobalActions.isReplaceRecoveryIcon;
import static sb.firefds.q.firefdskit.utils.Constants.RECOVERY_ACTION;

class RestartRecoveryActionViewModel extends RestartActionViewModel {

    RestartRecoveryActionViewModel() {

        super();
        getActionInfo().setName(RECOVERY_ACTION);
        getActionInfo().setLabel(getCustomRecovery() == null ? getResources().getString(R.string.reboot_recovery)
                : getCustomRecovery());
        getActionInfo().setDescription(getCustomRecoveryConfirmation() == null ? getResources().getString(R.string.reboot_confirm_recovery)
                : getCustomRecoveryConfirmation());
        setDrawableIcon(isReplaceRecoveryIcon() ? getResources().getDrawable(R.drawable.tw_ic_do_restart, null)
                : getResources().getDrawable(R.drawable.tw_ic_do_recovery_stock, null));
        setRebootOption(RECOVERY_ACTION);
    }
}
