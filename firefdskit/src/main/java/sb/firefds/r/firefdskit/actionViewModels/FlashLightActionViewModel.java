/*
 * Copyright (C) 2021 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.r.firefdskit.actionViewModels;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import sb.firefds.r.firefdskit.R;

import static sb.firefds.r.firefdskit.XSysUIGlobalActions.getFlashlightObject;
import static sb.firefds.r.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.r.firefdskit.utils.Constants.FLASHLIGHT_ACTION;

public class FlashLightActionViewModel extends FirefdsKitActionViewModel {
    private static boolean mTorch;

    FlashLightActionViewModel() {

        super();
        getActionInfo().setName(FLASHLIGHT_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.flashlight));
        setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.tw_ic_do_torchlight_stock, null));
        setStateLabel();
    }

    @Override
    public void onPress() {

        getGlobalActions().dismissDialog(false);
        switchFlashLight();
    }

    @Override
    public void onPressSecureConfirm() {
        switchFlashLight();
    }

    private void switchFlashLight() {
        mTorch = !mTorch;
        try {
            XposedHelpers.callMethod(getFlashlightObject(), "setFlashlight", mTorch);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        this.getGlobalActions().dismissDialog(false);
    }

    private void setStateLabel() {
        getActionInfo().setStateLabel(mTorch ? getResources().getString(R.string.flashlight_on) :
                getResources().getString(R.string.flashlight_off));
    }
}
