package sb.firefds.pie.firefdskit.actionViewModels;

import android.graphics.drawable.Drawable;

import androidx.annotation.Keep;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

@Keep
public class FlashLightActionViewModel extends FirefdsKitActionViewModel {
    private static boolean mTorch;
    private Object flashlightObject;
    private String flashlightOnStr;
    private String flashlightOffStr;

    FlashLightActionViewModel(Map<String, Object> actionViewModelDefaults,
                              String actionName,
                              String actionLabel,
                              String actionDescription,
                              Drawable actionIcon,
                              Object flashlightObject,
                              String flashlightOnStr,
                              String flashlightOffStr) {

        super(actionViewModelDefaults, actionName, actionLabel, actionDescription, actionIcon);
        this.flashlightObject = flashlightObject;
        this.flashlightOnStr = flashlightOnStr;
        this.flashlightOffStr = flashlightOffStr;
        setStateLabel();
    }

    @Override
    public void onPress() {

        getmGlobalActions().dismissDialog(false);
        switchFlashLight();
    }

    @Override
    public void onPressSecureConfirm() {
        switchFlashLight();
    }

    private void switchFlashLight() {
        mTorch = !mTorch;
        try {
            XposedHelpers.callMethod(flashlightObject, "setFlashlight", mTorch);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        getmGlobalActions().dismissDialog(false);
    }

    private void setStateLabel() {
        getActionInfo().setStateLabel(mTorch ? flashlightOnStr : flashlightOffStr);
    }
}
