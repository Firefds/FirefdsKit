package sb.firefds.pie.firefdskit.actionViewModels;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Process;

import androidx.annotation.Keep;

import java.util.Map;

@Keep
public class RestartSystemUiActionViewModel extends FirefdsKitActionViewModel {

    RestartSystemUiActionViewModel(Map<String, Object> actionViewModelDefaults,
                                   String actionName,
                                   String actionLabel,
                                   String actionDescription,
                                   Drawable actionIcon) {

        super(actionViewModelDefaults, actionName, actionLabel, actionDescription, actionIcon);
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
