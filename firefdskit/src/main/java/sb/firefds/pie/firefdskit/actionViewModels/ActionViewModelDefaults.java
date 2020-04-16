package sb.firefds.pie.firefdskit.actionViewModels;

import android.content.Context;

import com.samsung.android.globalactions.presentation.SecGlobalActionsPresenter;
import com.samsung.android.globalactions.presentation.features.FeatureFactory;
import com.samsung.android.globalactions.util.ConditionChecker;
import com.samsung.android.globalactions.util.KeyGuardManagerWrapper;

public class ActionViewModelDefaults {

    private Object context;
    private SecGlobalActionsPresenter globalActions;
    private FeatureFactory featureFactory;
    private ConditionChecker conditionChecker;
    private KeyGuardManagerWrapper keyGuardManagerWrapper;

    public ActionViewModelDefaults(Object context,
                                   SecGlobalActionsPresenter globalActions,
                                   FeatureFactory featureFactory,
                                   ConditionChecker conditionChecker,
                                   KeyGuardManagerWrapper keyGuardManagerWrapper) {
        this.context = context;
        this.globalActions = globalActions;
        this.featureFactory = featureFactory;
        this.conditionChecker = conditionChecker;
        this.keyGuardManagerWrapper = keyGuardManagerWrapper;
    }

    Context getContext() {
        return (Context) context;
    }

    SecGlobalActionsPresenter getGlobalActions() {
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