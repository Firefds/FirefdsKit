package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;

import de.robv.android.xposed.XposedBridge;

public class ScreenRecordActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private Context mContext;

    public ScreenRecordActionViewModel(Map<String, Object> actionViewModelDefaults) {
        mGlobalActions = (
                SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
        mContext = (Context) actionViewModelDefaults.get("mContext");
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {

        mGlobalActions.dismissDialog(false);
        startScreenRecord();
    }

    public void onPressSecureConfirm() {
        startScreenRecord();
    }

    private void startScreenRecord() {
        String str = "com.sec.app.screenrecorder/com.sec.app.screenrecorder.activity.LauncherActivity";
        if (!str.isEmpty()) {
            String[] split = str.split("/");
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName(split[0], split[1]));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                this.mGlobalActions.dismissDialog(false);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                XposedBridge.log("Activity not found - ScreenCapture");
            }
        }
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
