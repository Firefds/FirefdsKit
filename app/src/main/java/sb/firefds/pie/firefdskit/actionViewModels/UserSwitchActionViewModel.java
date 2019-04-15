package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Intent;

import java.util.Map;

import sb.firefds.pie.firefdskit.utils.Packages;

public class UserSwitchActionViewModel extends FirefdsKitActionViewModel {

    public UserSwitchActionViewModel(Map<String, Object> actionViewModelDefaults) {
        super(actionViewModelDefaults);
    }

    @Override
    public void onPress() {

        getmGlobalActions().dismissDialog(false);
        showUserSwitchScreen();
    }

    @Override
    public void onPressSecureConfirm() {
        showUserSwitchScreen();
    }

    private void showUserSwitchScreen() {
        Intent rebootIntent = new Intent("android.settings.USER_SETTINGS")
                .setPackage(Packages.SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getmContext().startActivity(rebootIntent);
    }
}
