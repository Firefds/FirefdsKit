package sb.firefds.pie.firefdskit.actionViewModels;

import android.os.Handler;
import android.os.Process;

import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.pie.firefdskit.utils.Constants.RESTART_UI_ACTION;

public class RestartSystemUiActionViewModel extends FirefdsKitActionViewModel {

    RestartSystemUiActionViewModel() {

        super();
        getActionInfo().setName(RESTART_UI_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.restartUI));
        getActionInfo().setDescription(getResources().getString(R.string.restartUI));
        setDrawableIcon(getResources().getDrawable(R.drawable.tw_ic_do_restart_ui_stock, null));
    }

    @Override
    public void onPress() {
        if (!getGlobalActions().isActionConfirming()) {
            getGlobalActions().confirmAction(this);
        } else {
            getGlobalActions().dismissDialog(false);
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
