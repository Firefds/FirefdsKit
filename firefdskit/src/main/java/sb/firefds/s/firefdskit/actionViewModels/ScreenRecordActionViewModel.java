/*
 * Copyright (C) 2022 Shauli Bracha for Firefds Kit Project (Firefds@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sb.firefds.s.firefdskit.actionViewModels;

import android.content.ComponentName;
import android.content.Intent;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.XposedBridge;
import sb.firefds.s.firefdskit.R;

import static sb.firefds.s.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.s.firefdskit.utils.Constants.SCREEN_RECORD_ACTION;
import static sb.firefds.s.firefdskit.utils.Packages.SMART_CAPTURE;

public class ScreenRecordActionViewModel extends FirefdsKitActionViewModel {

    private static final String SCREEN_RECORDER_SERVICE = "com.samsung.android.app.screenrecorder" +
            ".ScreenRecorderService";

    ScreenRecordActionViewModel() {

        super();
        getActionInfo().setName(SCREEN_RECORD_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.screen_record));
        setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.tw_ic_do_screenrecord_stock, null));
    }

    @Override
    public void onPress() {

        getGlobalActions().dismissDialog(false);
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
        if (getContext().startService(intent) == null) {
            XposedBridge.log("FFK: Service not found - ScreenRecorderService");
        }
    }
}
