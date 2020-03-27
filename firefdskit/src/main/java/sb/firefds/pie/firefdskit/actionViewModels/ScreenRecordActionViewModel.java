package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;

import androidx.annotation.Keep;

import java.util.HashMap;

import de.robv.android.xposed.XposedBridge;

import static sb.firefds.pie.firefdskit.utils.Packages.SCREEN_RECORDER;
import static sb.firefds.pie.firefdskit.utils.Packages.SMART_CAPTURE;

@Keep
public class ScreenRecordActionViewModel extends FirefdsKitActionViewModel {

    private static final String SCREEN_RECORDER_ACTIVITY = "com.sec.app.screenrecorder.activity.LauncherActivity";
    private static final String SCREEN_RECORDER_SERVICE = "com.samsung.android.app.screenrecorder.ScreenRecorderService";

    public ScreenRecordActionViewModel(HashMap<String, Object> actionViewModelDefaults) {
        super(actionViewModelDefaults);
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
            XposedBridge.log("FFK: Service not found - ScreenRecorderService. Trying external recorder");

            intent = new Intent("android.intent.action.MAIN")
                    .setComponent(new ComponentName(SCREEN_RECORDER, SCREEN_RECORDER_ACTIVITY))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                getmContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                XposedBridge.log("FFK: No screen recorder found");
            }
        }
    }
}
