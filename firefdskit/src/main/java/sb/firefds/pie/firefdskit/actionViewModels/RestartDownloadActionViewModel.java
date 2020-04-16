package sb.firefds.pie.firefdskit.actionViewModels;

import sb.firefds.pie.firefdskit.R;

import static sb.firefds.pie.firefdskit.XSysUIGlobalActions.getResources;
import static sb.firefds.pie.firefdskit.utils.Constants.DOWNLOAD_ACTION;

class RestartDownloadActionViewModel extends RestartActionViewModel {

    RestartDownloadActionViewModel() {

        super();
        getActionInfo().setName(DOWNLOAD_ACTION);
        getActionInfo().setLabel(getResources().getString(R.string.reboot_download));
        getActionInfo().setDescription(getResources().getString(R.string.reboot_confirm_download));
        setDrawableIcon(getResources().getDrawable(R.drawable.tw_ic_do_download_stock, null));
        setRebootOption(DOWNLOAD_ACTION);
    }
}
