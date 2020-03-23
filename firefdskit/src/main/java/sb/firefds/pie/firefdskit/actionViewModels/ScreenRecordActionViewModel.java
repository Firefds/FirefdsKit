package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;

import androidx.annotation.Keep;

import java.util.HashMap;

import de.robv.android.xposed.XposedBridge;

import static sb.firefds.pie.firefdskit.utils.Packages.SCREEN_RECORDER;

@Keep
public class ScreenRecordActionViewModel extends FirefdsKitActionViewModel {

    private static final String SCREEN_RECORDER_ACTIVITY = "com.sec.app.screenrecorder.activity.LauncherActivity";

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
        Intent intent = new Intent("android.intent.action.MAIN")
                .setComponent(new ComponentName(SCREEN_RECORDER, SCREEN_RECORDER_ACTIVITY))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getmContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            XposedBridge.log("Activity not found - ScreenCapture");
            XposedBridge.log(e);
        }
    }
}
