package sb.firefds.pie.firefdskit.actionViewModels;

import android.os.Handler;
import android.os.Process;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;

public class RestartSystemUiActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;

    public RestartSystemUiActionViewModel(Map<String, Object> actionViewModelDefaults) {
        mGlobalActions = (
                SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {
        mGlobalActions.dismissDialog(false);
        new Handler().postDelayed(this::restartSystemUI, 1000);
    }

    public void onPressSecureConfirm() {
        restartSystemUI();
    }

    private void restartSystemUI() {
        Process.killProcess(Process.myPid());
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
