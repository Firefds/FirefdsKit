package sb.firefds.q.firefdskit.actionViewModels;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import de.robv.android.xposed.XposedBridge;

import static sb.firefds.q.firefdskit.utils.Packages.SMART_CAPTURE;

public class ScreenRecordActionViewModel extends FirefdsKitActionViewModel {

    private static final String SCREEN_RECORDER_SERVICE = "com.samsung.android.app.screenrecorder.ScreenRecorderService";

    ScreenRecordActionViewModel(ActionViewModelDefaults actionViewModelDefaults,
                                String actionName,
                                String actionLabel,
                                String actionDescription,
                                Drawable actionIcon) {

        super(actionViewModelDefaults, actionName, actionLabel, actionDescription, actionIcon);
    }

    @Override
    public void onPress() {

        getmGlobalActions().dismissDialog(false);
        startScreenRecord();
    }

    @Override
    public void onPressSecureConfirm() {
        startScreenRecord();
    }

    private void startScreenRecord() {
        Intent intent = new Intent()
                .setComponent(new ComponentName(SMART_CAPTURE, SCREEN_RECORDER_SERVICE))
                .setAction("com.samsung.android.app.screenrecorder.ACTION_START");
        if (getmContext().startService(intent) == null) {
            XposedBridge.log("FFK: Service not found - ScreenRecorderService");
        }
    }
}
