package sb.firefds.pie.firefdskit.actionViewModels;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class FlashLightActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private static boolean mTorch;
    private static Object flashlightObject;
    private static String flashlightOnStr;
    private static String flashlightOffStr;


    public FlashLightActionViewModel(Map<String, Object> actionViewModelDefaults,
                                     Object flashlight,
                                     String flashlightOn,
                                     String flashlightOff) {
        mGlobalActions = (
                SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
        flashlightObject = flashlight;
        flashlightOnStr = flashlightOn;
        flashlightOffStr = flashlightOff;
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {

        mGlobalActions.dismissDialog(false);
        switchFlashLight();
    }

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
        this.mGlobalActions.dismissDialog(false);
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
        setStateLabel();
    }

    private void setStateLabel() {
        this.mInfo.setStateLabel(mTorch ? flashlightOnStr : flashlightOffStr);
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
