package sb.firefds.pie.firefdskit.actionViewModels;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

public class ScreenShotActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;

    public ScreenShotActionViewModel(Map<String, Object> actionViewModelDefaults) {
        mGlobalActions = (
                SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {

        mGlobalActions.dismissDialog(false);
        takeScreenshot();
    }

    public void onPressSecureConfirm() {
        takeScreenshot();
    }

    private void takeScreenshot() {
        int[] arrayOfInt = new int[2];
        arrayOfInt[1] = 1;
        for (int k : arrayOfInt) {
            long l = SystemClock.uptimeMillis();
            final InputManager inputManager = (InputManager)
                    XposedHelpers.callStaticMethod(InputManager.class, "getInstance");
            XposedHelpers.callMethod(inputManager,
                    "injectInputEvent",
                    new KeyEvent(l, l, k, 120, 0, 0, -1, 0, 268435464, 257), 0);
        }
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
