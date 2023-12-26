/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit.actionViewModels;

import static sb.firefds.u.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.u.firefdskit.utils.Constants.DOWNLOAD_ACTION;

import androidx.core.content.res.ResourcesCompat;

import sb.firefds.u.firefdskit.R;

class RestartDownloadActionViewModel extends RestartActionViewModel {

    RestartDownloadActionViewModel() {

        super();
        getActionInfo().setName(DOWNLOAD_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.reboot_download));
        getActionInfo().setDescription(getResources().getString(R.string.reboot_confirm_download));
        setDrawableIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.tw_ic_do_download_stock, null));
        setRebootOption(DOWNLOAD_ACTION);
    }
}
