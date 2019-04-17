package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;
import android.os.PowerManager;

import androidx.annotation.Keep;

@Keep
public class RestartActionViewModel extends FirefdsKitActionViewModel {
    private String rebootOption;

    public RestartActionViewModel(Object[] actionViewModelDefaults) {
        super(actionViewModelDefaults);
    }

    @Override
    public void onPress() {

        if (!getmGlobalActions().isActionConfirming()) {
            getmGlobalActions().confirmAction(this);
        } else {
            reboot();
        }
    }

    @Override
    public void onPressSecureConfirm() {
        reboot();
    }

    private void reboot() {
        ((PowerManager) getmContext().getSystemService(Context.POWER_SERVICE)).reboot(rebootOption);
    }

    public void setRebootOption(String rebootOption) {
        this.rebootOption = rebootOption;
    }
}
