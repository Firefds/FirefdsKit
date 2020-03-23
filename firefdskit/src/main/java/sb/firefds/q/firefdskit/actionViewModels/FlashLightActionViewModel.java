package sb.firefds.q.firefdskit.actionViewModels;

import androidx.annotation.Keep;

import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;

import java.util.HashMap;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

@Keep
public class FlashLightActionViewModel extends FirefdsKitActionViewModel {
    private static boolean mTorch;
    private static Object flashlightObject;
    private static String flashlightOnStr;
    private static String flashlightOffStr;

    public FlashLightActionViewModel(HashMap<String, Object> actionViewModelDefaults) {
        super(actionViewModelDefaults);
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

    @Override
    public void setActionInfo(ActionInfo var1) {
        super.setActionInfo(var1);
        setStateLabel();
    }

    private void setStateLabel() {
        this.getmInfo().setStateLabel(mTorch ? flashlightOnStr : flashlightOffStr);
    }

    public static void setFlashlightObject(Object flashlightObject) {
        FlashLightActionViewModel.flashlightObject = flashlightObject;
    }

    public static void setFlashlightOnStr(String flashlightOnStr) {
        FlashLightActionViewModel.flashlightOnStr = flashlightOnStr;
    }

    public static void setFlashlightOffStr(String flashlightOffStr) {
        FlashLightActionViewModel.flashlightOffStr = flashlightOffStr;
    }
}
