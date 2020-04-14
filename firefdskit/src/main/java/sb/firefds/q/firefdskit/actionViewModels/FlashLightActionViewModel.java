package sb.firefds.q.firefdskit.actionViewModels;

import android.graphics.drawable.Drawable;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class FlashLightActionViewModel extends FirefdsKitActionViewModel {
    private static boolean mTorch;
    private Object flashlightObject;
    private String flashlightOnStr;
    private String flashlightOffStr;

    FlashLightActionViewModel(ActionViewModelDefaults actionViewModelDefaults,
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
        this.getmGlobalActions().dismissDialog(false);
    }

    private void setStateLabel() {
        getActionInfo().setStateLabel(mTorch ? flashlightOnStr : flashlightOffStr);
    }
}
