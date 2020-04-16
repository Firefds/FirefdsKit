package sb.firefds.q.firefdskit.actionViewModels;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.KeyEvent;

import de.robv.android.xposed.XposedHelpers;
import sb.firefds.q.firefdskit.R;

import static sb.firefds.q.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.q.firefdskit.utils.Constants.SCREENSHOT_ACTION;

public class ScreenShotActionViewModel extends FirefdsKitActionViewModel {

    ScreenShotActionViewModel() {

        super();
        getActionInfo().setName(SCREENSHOT_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.screenshot));
        setDrawableIcon(getResources().getDrawable(R.drawable.tw_ic_do_screenshot_stock, null));
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
