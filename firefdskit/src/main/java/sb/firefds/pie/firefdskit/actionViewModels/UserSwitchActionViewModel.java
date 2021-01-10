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

import android.content.Intent;

import androidx.core.content.res.ResourcesCompat;

import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.pie.firefdskit.utils.Constants.MULTIUSER_ACTION;
import static sb.firefds.pie.firefdskit.utils.Packages.SETTINGS;

public class UserSwitchActionViewModel extends FirefdsKitActionViewModel {

    UserSwitchActionViewModel() {

        super();
        getActionInfo().setName(MULTIUSER_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.switchUser));
        setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.tw_ic_do_users_stock, null));
    }

    @Override
    public void onPress() {

        getGlobalActions().dismissDialog(false);
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
        getContext().startActivity(rebootIntent);
    }
}
