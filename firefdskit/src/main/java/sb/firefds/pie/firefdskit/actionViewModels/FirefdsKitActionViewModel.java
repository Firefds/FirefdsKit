/*
 * Copyright (C) 2020 Shauli Bracha for FirefdsKit Project (firefds@xda)
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
package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.presentation.viewmodel.ViewType;

import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getActionViewModelDefaults;

public abstract class FirefdsKitActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo = new ActionInfo();
    private final Context mContext;
    private BitmapDrawable mIcon;

    FirefdsKitActionViewModel() {

        mContext = getActionViewModelDefaults().getContext().get();
        mGlobalActions = getActionViewModelDefaults().getGlobalActions();
        mInfo.setViewType(ViewType.CENTER_ICON_3P_VIEW);
    }

    @Override
    public ActionInfo getActionInfo() {
        return mInfo;
    }

    @Override
    public abstract void onPress();

    @Override
    public abstract void onPressSecureConfirm();

    @Override
    public void setActionInfo(ActionInfo var1) {
        mInfo = var1;
    }

    @Override
    public boolean showBeforeProvisioning() {
        return true;
    }

    @Override
    public BitmapDrawable getIcon() {
        return mIcon;
    }

    void setDrawableIcon(Drawable mIcon) {
        this.mIcon = (BitmapDrawable) mIcon;
    }

    SecGlobalActions getGlobalActions() {
        return mGlobalActions;
    }

    Context getContext() {
        return mContext;
    }
}
