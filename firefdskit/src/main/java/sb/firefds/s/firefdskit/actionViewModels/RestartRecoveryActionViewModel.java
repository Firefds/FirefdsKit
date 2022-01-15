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

import androidx.core.content.res.ResourcesCompat;

import sb.firefds.s.firefdskit.R;

import static sb.firefds.s.firefdskit.XSysUIGlobalActions.getCustomRecovery;
import static sb.firefds.s.firefdskit.XSysUIGlobalActions.getCustomRecoveryConfirmation;
import static sb.firefds.s.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.s.firefdskit.XSysUIGlobalActions.isReplaceRecoveryIcon;
import static sb.firefds.s.firefdskit.utils.Constants.RECOVERY_ACTION;

class RestartRecoveryActionViewModel extends RestartActionViewModel {

    RestartRecoveryActionViewModel() {

        super();
        getActionInfo().setName(RECOVERY_ACTION);
        getActionInfo().setLabel(getCustomRecovery() == null ? getResources().getString(R.string.reboot_recovery)
                : getCustomRecovery());
        getActionInfo().setDescription(getCustomRecoveryConfirmation() == null ?
                getResources().getString(R.string.reboot_confirm_recovery)
                : getCustomRecoveryConfirmation());
        setDrawableIcon(isReplaceRecoveryIcon() ? ResourcesCompat.getDrawable(getResources(),
                R.drawable.tw_ic_do_restart, null)
                : ResourcesCompat.getDrawable(getResources(), R.drawable.tw_ic_do_recovery_stock, null));
        setRebootOption(RECOVERY_ACTION);
    }
}
