package sb.firefds.pie.firefdskit.actionViewModels;

import android.os.Handler;
import android.os.Process;

import java.util.Map;

public class RestartSystemUiActionViewModel extends FirefdsKitActionViewModel {

    public RestartSystemUiActionViewModel(Map<String, Object> actionViewModelDefaults) {
        super(actionViewModelDefaults);
    }

    @Override
    public void onPress() {
        if (!getmGlobalActions().isActionConfirming()) {
            getmGlobalActions().confirmAction(this);
        } else {
            getmGlobalActions().dismissDialog(false);
            new Handler().postDelayed(this::restartSystemUI, 1000);
        }
    }

    @Override
    public void onPressSecureConfirm() {
        restartSystemUI();
    }

    private void restartSystemUI() {
        Process.killProcess(Process.myPid());
    }
}
