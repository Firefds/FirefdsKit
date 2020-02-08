package sb.firefds.q.firefdskit.actionViewModels;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;

import androidx.annotation.Keep;

import de.robv.android.xposed.XposedBridge;

import static sb.firefds.q.firefdskit.utils.Packages.SCREEN_RECORDER;
import static sb.firefds.q.firefdskit.utils.Packages.SYSTEM_UI;

@Keep
public class ScreenRecordActionViewModel extends FirefdsKitActionViewModel {

    private static final String SCREEN_RECORDER_ACTIVITY = "com.sec.app.screenrecorder.activity.LauncherActivity";
    private static final String NATIVE_SCREEN_RECORDER_ACTIVITY = SYSTEM_UI + ".screenrecord.ScreenRecordDialog";

    public ScreenRecordActionViewModel(Object[] actionViewModelDefaults) {
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
            XposedBridge.log(e);
            XposedBridge.log("Activity not found - ScreenCapture. Trying natively");
            Intent intent2 = new Intent()
                    .setComponent(new ComponentName(SYSTEM_UI, NATIVE_SCREEN_RECORDER_ACTIVITY))
                    .setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            try {
                getmContext().startActivity(intent2);
            } catch (Throwable e1) {
                XposedBridge.log(e1);
            }
        }
    }
}
