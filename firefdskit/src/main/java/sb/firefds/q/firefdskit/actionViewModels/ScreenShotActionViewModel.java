package sb.firefds.q.firefdskit.actionViewModels;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import androidx.annotation.Keep;

import java.util.HashMap;

import de.robv.android.xposed.XposedHelpers;

@Keep
public class ScreenShotActionViewModel extends FirefdsKitActionViewModel {

    public ScreenShotActionViewModel(HashMap<String, Object> actionViewModelDefaults) {
        super(actionViewModelDefaults);
    }

    @Override
    public void onPress() {

        getmGlobalActions().dismissDialog(false);
        takeScreenshot();
    }

    @Override
    public void onPressSecureConfirm() {
        takeScreenshot();
    }

    private void takeScreenshot() {
        int[] arrayOfInt = new int[2];
        arrayOfInt[1] = 1;
        for (int k : arrayOfInt) {
            long l = SystemClock.uptimeMillis();
            final InputManager inputManager = (InputManager) XposedHelpers.callStaticMethod(InputManager.class, "getInstance");
            XposedHelpers.callMethod(inputManager,
                    "injectInputEvent",
                    new KeyEvent(l, l, k, 120, 0, 0, -1, 0, 268435464, 257), 0);
        }
    }
}
