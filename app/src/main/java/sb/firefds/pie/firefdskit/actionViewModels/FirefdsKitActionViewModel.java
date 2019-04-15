package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;


public class FirefdsKitActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private Context mContext;

    FirefdsKitActionViewModel(Map<String, Object> actionViewModelDefaults) {
        mGlobalActions = (
                SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
        mContext = (Context) actionViewModelDefaults.get("mContext");
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {
    }

    public void onPressSecureConfirm() {
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }

    SecGlobalActions getmGlobalActions() {
        return mGlobalActions;
    }

    ActionInfo getmInfo() {
        return mInfo;
    }

    Context getmContext() {
        return mContext;
    }
}
