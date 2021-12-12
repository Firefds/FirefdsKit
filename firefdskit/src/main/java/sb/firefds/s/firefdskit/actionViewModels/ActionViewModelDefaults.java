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
package sb.firefds.s.firefdskit.actionViewModels;

import android.content.Context;

import com.samsung.android.globalactions.presentation.SamsungGlobalActionsPresenter;
import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;

import java.lang.ref.WeakReference;

public class ActionViewModelDefaults {

    private final WeakReference<Context> context;
    private final SamsungGlobalActionsPresenter globalActions;
    private final FeatureFactory featureFactory;
    private final ConditionChecker conditionChecker;
    private final KeyGuardManagerWrapper keyGuardManagerWrapper;

    public ActionViewModelDefaults(WeakReference<Context> context,
                                   SamsungGlobalActionsPresenter globalActions,
                                   FeatureFactory featureFactory,
                                   ConditionChecker conditionChecker,
                                   KeyGuardManagerWrapper keyGuardManagerWrapper) {
        this.context = context;
        this.globalActions = globalActions;
        this.featureFactory = featureFactory;
        this.conditionChecker = conditionChecker;
        this.keyGuardManagerWrapper = keyGuardManagerWrapper;
    }

    WeakReference<Context> getContext() {
        return context;
    }

    SamsungGlobalActionsPresenter getGlobalActions() {
        return globalActions;
    }

    FeatureFactory getFeatureFactory() {
        return featureFactory;
    }

    ConditionChecker getConditionChecker() {
        return conditionChecker;
    }

    KeyGuardManagerWrapper getKeyGuardManagerWrapper() {
        return keyGuardManagerWrapper;
    }
}
