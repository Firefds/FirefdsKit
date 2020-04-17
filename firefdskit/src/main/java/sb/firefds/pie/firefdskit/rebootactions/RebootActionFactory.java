package sb.firefds.pie.firefdskit.rebootactions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static sb.firefds.pie.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.QUICK_REBOOT_DEVICE_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.REBOOT_DEVICE_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.RECOVERY_ACTION;

public class RebootActionFactory {

    private static final Map<String, Function<AppCompatActivity, RebootAction>> REBOOT_ACTION_MAP = new HashMap<>();

    private static final Function<AppCompatActivity, RebootAction> NORMAL_REBOOT_ACTION = NormalRebootDeviceAction::new;
    private static final Function<AppCompatActivity, RebootAction> QUICK_REBOOT_ACTION = QuickRebootDeviceAction::new;
    private static final Function<AppCompatActivity, RebootAction> RECOVERY_REBOOT_ACTION = (o) -> new RecoveryRebootAction();
    private static final Function<AppCompatActivity, RebootAction> DOWNLOAD_REBOOT_ACTION = (o) -> new DownloadRebootAction();

    static {
        REBOOT_ACTION_MAP.put(REBOOT_DEVICE_ACTION, NORMAL_REBOOT_ACTION);
        REBOOT_ACTION_MAP.put(QUICK_REBOOT_DEVICE_ACTION, QUICK_REBOOT_ACTION);
        REBOOT_ACTION_MAP.put(RECOVERY_ACTION, RECOVERY_REBOOT_ACTION);
        REBOOT_ACTION_MAP.put(DOWNLOAD_ACTION, DOWNLOAD_REBOOT_ACTION);
    }

    public static RebootAction getRebootAction(String rebootAction, AppCompatActivity activity) {
        return Objects.requireNonNull(REBOOT_ACTION_MAP.get(rebootAction)).apply(activity);
    }
}
