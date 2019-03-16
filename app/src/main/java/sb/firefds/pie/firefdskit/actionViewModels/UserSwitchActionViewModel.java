package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;
import android.content.Intent;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.Map;

import sb.firefds.pie.firefdskit.utils.Packages;

public class UserSwitchActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo;
    private Context mContext;

    public UserSwitchActionViewModel(Map<String, Object> actionViewModelDefaults) {
        mGlobalActions = (
                SecGlobalActions) actionViewModelDefaults.get("mSecGlobalActionsPresenter");
        mContext = (Context) actionViewModelDefaults.get("mContext");
    }

    public ActionInfo getActionInfo() {
        return mInfo;
    }

    public void onPress() {

        mGlobalActions.dismissDialog(false);
        showUserSwitchScreen();
    }

    public void onPressSecureConfirm() {
        showUserSwitchScreen();
    }

    private void showUserSwitchScreen() {
        Intent rebootIntent = new Intent("android.settings.USER_SETTINGS")
                .setPackage(Packages.SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(rebootIntent);
    }

    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    public boolean showBeforeProvisioning() {
        return true;
    }
}
