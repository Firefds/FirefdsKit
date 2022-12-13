package sb.firefds.q.firefdskit.features;

import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private Date securityPatch;
    private Date december;

    public Utils() {
        initDates();
    }

    private void initDates() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            securityPatch = format.parse(Build.VERSION.SECURITY_PATCH);
            december = format.parse("2022-12-01");
        } catch (ParseException ignored) {
        }
    }

    public String getComAndroidSystemui_NotificationPanelViewControllerClassName() {
        return isSecurityPatchAfterDecember2022() ? "com.android.systemui.shade.NotificationPanelViewController" : "com.android.systemui.statusbar.phone.NotificationPanelViewController";
    }

    public boolean isSecurityPatchAfterDecember2022() {
        return securityPatch.after(december);
    }
}
