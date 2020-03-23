package sb.firefds.q.firefdskit.actionViewModels;

import android.content.Intent;

import androidx.annotation.Keep;

import java.util.HashMap;

import static sb.firefds.q.firefdskit.utils.Packages.SETTINGS;

@Keep
public class UserSwitchActionViewModel extends FirefdsKitActionViewModel {

    public UserSwitchActionViewModel(HashMap<String, Object> actionViewModelDefaults) {
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
                .setPackage(SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getmContext().startActivity(rebootIntent);
    }
}
