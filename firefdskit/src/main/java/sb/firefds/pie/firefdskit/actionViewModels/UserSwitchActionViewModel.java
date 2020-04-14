package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.annotation.Keep;

import java.util.Map;

import static sb.firefds.pie.firefdskit.utils.Packages.SETTINGS;

@Keep
public class UserSwitchActionViewModel extends FirefdsKitActionViewModel {

    UserSwitchActionViewModel(Map<String, Object> actionViewModelDefaults,
                              String actionName,
                              String actionLabel,
                              String actionDescription,
                              Drawable actionIcon) {

        super(actionViewModelDefaults, actionName, actionLabel, actionDescription, actionIcon);
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
