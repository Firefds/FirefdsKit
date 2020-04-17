package sb.firefds.pie.firefdskit.rebootactions;

import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.RECOVERY_ACTION;

public class RecoveryRebootAction implements RebootAction {

    @Override
    public void reboot() {
        Utils.rebootEPM(RECOVERY_ACTION);
    }
}
