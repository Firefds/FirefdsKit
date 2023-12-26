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

import static sb.firefds.u.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.FLASHLIGHT_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.MULTIUSER_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.RECOVERY_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.RESTART_UI_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.SCREENSHOT_ACTION;
import static sb.firefds.u.firefdskit.utils.Constants.SCREEN_RECORD_ACTION;

import androidx.core.util.Supplier;

import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FirefdsKitActionViewModelsFactory {

    private static final Map<String, Supplier<ActionViewModel>> ACTION_VIEW_MODEL_MAP = new HashMap<>();

    private static final Supplier<ActionViewModel>
            FLASH_LIGHT_ACTION_VIEW_MODEL = FlashLightActionViewModel::new;

    private static final Supplier<ActionViewModel>
            SCREEN_RECORD_ACTION_VIEW_MODEL = ScreenRecordActionViewModel::new;

    private static final Supplier<ActionViewModel>
            RESTART_SYSTEM_UI_ACTION_VIEW_MODEL = RestartSystemUiActionViewModel::new;

    private static final Supplier<ActionViewModel>
            MULTIUSER_ACTION_VIEW_MODEL = UserSwitchActionViewModel::new;

    private static final Supplier<ActionViewModel>
            SCREENSHOT_ACTION_VIEW_MODEL = ScreenShotActionViewModel::new;

    private static final Supplier<ActionViewModel>
            RESTART_DOWNLOAD_ACTION_VIEW_MODEL = RestartDownloadActionViewModel::new;

    private static final Supplier<ActionViewModel>
            RESTART_RECOVERY_ACTION_VIEW_MODEL = RestartRecoveryActionViewModel::new;

    static {
        ACTION_VIEW_MODEL_MAP.put(FLASHLIGHT_ACTION, FLASH_LIGHT_ACTION_VIEW_MODEL);
        ACTION_VIEW_MODEL_MAP.put(SCREEN_RECORD_ACTION, SCREEN_RECORD_ACTION_VIEW_MODEL);
        ACTION_VIEW_MODEL_MAP.put(RESTART_UI_ACTION, RESTART_SYSTEM_UI_ACTION_VIEW_MODEL);
        ACTION_VIEW_MODEL_MAP.put(MULTIUSER_ACTION, MULTIUSER_ACTION_VIEW_MODEL);
        ACTION_VIEW_MODEL_MAP.put(SCREENSHOT_ACTION, SCREENSHOT_ACTION_VIEW_MODEL);
        ACTION_VIEW_MODEL_MAP.put(DOWNLOAD_ACTION, RESTART_DOWNLOAD_ACTION_VIEW_MODEL);
        ACTION_VIEW_MODEL_MAP.put(RECOVERY_ACTION, RESTART_RECOVERY_ACTION_VIEW_MODEL);
    }

    public static Optional<ActionViewModel> getActionViewModel(String action) {
        return Optional.ofNullable(ACTION_VIEW_MODEL_MAP.get(action))
                .map(Supplier::get);
    }
}
