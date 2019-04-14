package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;
import android.os.PowerManager;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;

public class RestartActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private Context mContext;
    private String rebootOption;

    public RestartActionViewModel(Map<String, Object> actionViewModelDefaults,
                                  String paramRebootOption) {
        mGlobalActions =
                (SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
        mContext = (Context) actionViewModelDefaults.get("mContext");
        rebootOption = paramRebootOption;
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {

        if (!mGlobalActions.isActionConfirming()) {
            mGlobalActions.confirmAction(this);
        } else {
            reboot();
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
