package sb.firefds.q.firefdskit.actionViewModels;

import android.content.ComponentName;
import android.content.Intent;

import de.robv.android.xposed.XposedBridge;
import sb.firefds.q.firefdskit.R;

import static sb.firefds.q.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.q.firefdskit.utils.Constants.SCREEN_RECORD_ACTION;
import static sb.firefds.q.firefdskit.utils.Packages.SMART_CAPTURE;

public class ScreenRecordActionViewModel extends FirefdsKitActionViewModel {

    private static final String SCREEN_RECORDER_SERVICE = "com.samsung.android.app.screenrecorder.ScreenRecorderService";

    ScreenRecordActionViewModel() {

        super();
        getActionInfo().setName(SCREEN_RECORD_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.screen_record));
        setDrawableIcon(getResources().getDrawable(R.drawable.tw_ic_do_screenrecord_stock, null));
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
