package sb.firefds.pie.firefdskit.rebootactions;

import androidx.appcompat.app.AppCompatActivity;

import sb.firefds.pie.firefdskit.utils.Utils;

class QuickRebootDeviceAction extends RebootDeviceAction implements RebootAction {

    QuickRebootDeviceAction(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void reboot() throws Throwable {
        Utils.closeStatusBar(getActivity());
        showRebootDialog();
        Utils.performQuickReboot();
    }
}
