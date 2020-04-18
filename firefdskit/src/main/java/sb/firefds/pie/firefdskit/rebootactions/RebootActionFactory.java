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
package sb.firefds.pie.firefdskit.rebootactions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static sb.firefds.pie.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.QUICK_REBOOT_DEVICE_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_DEVICE_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.RECOVERY_ACTION;

public class RebootActionFactory {

    private static final Map<String, Supplier<RebootAction>> REBOOT_ACTION_MAP = new HashMap<>();

    private static final Supplier<RebootAction> NORMAL_REBOOT_ACTION = NormalRebootDeviceAction::new;
    private static final Supplier<RebootAction> QUICK_REBOOT_ACTION = QuickRebootDeviceAction::new;
    private static final Supplier<RebootAction> RECOVERY_REBOOT_ACTION = RecoveryRebootAction::new;
    private static final Supplier<RebootAction> DOWNLOAD_REBOOT_ACTION = DownloadRebootAction::new;

    static {
        REBOOT_ACTION_MAP.put(REBOOT_DEVICE_ACTION, NORMAL_REBOOT_ACTION);
        REBOOT_ACTION_MAP.put(QUICK_REBOOT_DEVICE_ACTION, QUICK_REBOOT_ACTION);
        REBOOT_ACTION_MAP.put(RECOVERY_ACTION, RECOVERY_REBOOT_ACTION);
        REBOOT_ACTION_MAP.put(DOWNLOAD_ACTION, DOWNLOAD_REBOOT_ACTION);
    }

    public static Optional<RebootAction> getRebootAction(String rebootAction) {
        Optional<Supplier<RebootAction>> optional = Optional.ofNullable(REBOOT_ACTION_MAP.get(rebootAction));
        return optional.map(Supplier::get);
    }
}
