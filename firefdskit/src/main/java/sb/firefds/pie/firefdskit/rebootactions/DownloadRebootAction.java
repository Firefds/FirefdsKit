package sb.firefds.pie.firefdskit.rebootactions;

import sb.firefds.pie.firefdskit.utils.Utils;

import static sb.firefds.pie.firefdskit.utils.Constants.DOWNLOAD_ACTION;

public class DownloadRebootAction implements RebootAction {

    @Override
    public void reboot() {
        Utils.rebootEPM(DOWNLOAD_ACTION);
    }
}
