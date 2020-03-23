package sb.firefds.q.firefdskit.actionViewModels;

import android.os.Handler;
import android.os.Process;

import androidx.annotation.Keep;

import java.util.HashMap;

@Keep
public class RestartSystemUiActionViewModel extends FirefdsKitActionViewModel {

    public RestartSystemUiActionViewModel(HashMap<String, Object> actionViewModelDefaults) {
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
