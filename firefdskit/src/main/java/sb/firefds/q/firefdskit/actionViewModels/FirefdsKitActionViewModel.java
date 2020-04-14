package sb.firefds.q.firefdskit.actionViewModels;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.samsung.android.globalactions.presentation.SecGlobalActions;
import com.samsung.android.globalactions.presentation.viewmodel.ActionInfo;
import com.samsung.android.globalactions.presentation.viewmodel.ActionViewModel;
import com.samsung.android.globalactions.presentation.viewmodel.ViewType;

public abstract class FirefdsKitActionViewModel implements ActionViewModel {
    private final SecGlobalActions mGlobalActions;
    private ActionInfo mInfo = new ActionInfo();
    private Context mContext;
    private BitmapDrawable mIcon;

    FirefdsKitActionViewModel(ActionViewModelDefaults actionViewModelDefaults,
                              String actionName,
                              String actionLabel,
                              String actionDescription,
                              Drawable actionIcon) {

        mContext = actionViewModelDefaults.getContext();
        mGlobalActions = actionViewModelDefaults.getGlobalActions();
        mInfo.setName(actionName);
        mInfo.setLabel(actionLabel);
        mInfo.setDescription(actionDescription);
        mInfo.setViewType(ViewType.CENTER_ICON_3P_VIEW);
        mIcon = (BitmapDrawable) actionIcon;
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

    SecGlobalActions getmGlobalActions() {
        return mGlobalActions;
    }

    Context getmContext() {
        return mContext;
    }
}
