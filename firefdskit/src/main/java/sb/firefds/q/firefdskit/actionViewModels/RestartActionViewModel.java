package sb.firefds.q.firefdskit.actionViewModels;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.Keep;

import static sb.firefds.q.firefdskit.utils.Constants.REBOOT_ACTION;
import static sb.firefds.q.firefdskit.utils.Packages.FIREFDSKIT;

@Keep
public class RestartActionViewModel extends FirefdsKitActionViewModel {
    private static final String REBOOT_ACTIVITY = FIREFDSKIT + ".activities.WanamRebootActivity";
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
}
