package sb.firefds.pie.firefdskit.actionViewModels;

import android.graphics.drawable.Drawable;

import androidx.core.util.Supplier;

import java.util.HashMap;
import java.util.Map;

import static sb.firefds.pie.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.FLASHLIGHT_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.MULTIUSER_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.RECOVERY_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.RESTART_UI_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.SCREENSHOT_ACTION;
import static sb.firefds.pie.firefdskit.utils.Constants.SCREEN_RECORD_ACTION;

public class FirefdsKitActionViewModelsFactory {

    private static Object mFlashlightObject;
    private static Map<String, Drawable> actionIcons;
    private static Map<String, String> actionStrings;
    private static Map<String, Object> actionViewModelDefaults;
    private static boolean prefUnlockKeyguardBeforeActionExecute;

    private static final Map<String, Supplier<FirefdsKitActionViewModel>> actionViewModelMap = new HashMap<>();

    private static final Supplier<FirefdsKitActionViewModel> flashLightActionViewModel = () ->
            new FlashLightActionViewModel(actionViewModelDefaults,
                    FLASHLIGHT_ACTION,
                    actionStrings.get("mFlashlightStr"),
                    null,
                    actionIcons.get("mFlashLightIcon"),
                    mFlashlightObject,
                    actionStrings.get("mFlashlightOnStr"),
                    actionStrings.get("mFlashlightOffStr"));

    private static final Supplier<FirefdsKitActionViewModel> screenRecordActionViewModel = () ->
            new ScreenRecordActionViewModel(actionViewModelDefaults,
                    SCREEN_RECORD_ACTION,
                    actionStrings.get("mScreenRecordStr"),
                    null,
                    actionIcons.get("mScreenRecordIcon"));

    private static final Supplier<FirefdsKitActionViewModel> restartSystemUiActionViewModel = () ->
            new RestartSystemUiActionViewModel(actionViewModelDefaults,
                    RESTART_UI_ACTION,
                    actionStrings.get("mRestartSystemUiStr"),
                    actionStrings.get("mRestartSystemUiConfirmStr"),
                    actionIcons.get("mRestartSystemUiIcon"));

    private static final Supplier<FirefdsKitActionViewModel> userSwitchActionViewModel = () ->
            new UserSwitchActionViewModel(actionViewModelDefaults,
                    MULTIUSER_ACTION,
                    actionStrings.get("mSwitchUserStr"),
                    null,
                    actionIcons.get("mSwitchUserIcon"));

    private static final Supplier<FirefdsKitActionViewModel> screenShotActionViewModel = () ->
            new ScreenShotActionViewModel(actionViewModelDefaults,
                    SCREENSHOT_ACTION,
                    actionStrings.get("mScreenshotStr"),
                    null,
                    actionIcons.get("mScreenshotIcon"));

    private static final Supplier<FirefdsKitActionViewModel> restartDownloadActionViewModel = () ->
            new RestartActionViewModel(actionViewModelDefaults,
                    DOWNLOAD_ACTION,
                    actionStrings.get("mDownloadStr"),
                    actionStrings.get("mRebootConfirmDownloadStr"),
                    actionIcons.get("mDownloadIcon"),
                    prefUnlockKeyguardBeforeActionExecute);

    private static final Supplier<FirefdsKitActionViewModel> restartRecoveryActionViewModel = () ->
            new RestartActionViewModel(actionViewModelDefaults,
                    RECOVERY_ACTION,
                    actionStrings.get("mRecoveryStr"),
                    actionStrings.get("mRebootConfirmRecoveryStr"),
                    actionIcons.get("mRecoveryIcon"),
                    prefUnlockKeyguardBeforeActionExecute);


    static {
        actionViewModelMap.put(FLASHLIGHT_ACTION, flashLightActionViewModel);
        actionViewModelMap.put(SCREEN_RECORD_ACTION, screenRecordActionViewModel);
        actionViewModelMap.put(RESTART_UI_ACTION, restartSystemUiActionViewModel);
        actionViewModelMap.put(MULTIUSER_ACTION, userSwitchActionViewModel);
        actionViewModelMap.put(SCREENSHOT_ACTION, screenShotActionViewModel);
        actionViewModelMap.put(DOWNLOAD_ACTION, restartDownloadActionViewModel);
        actionViewModelMap.put(RECOVERY_ACTION, restartRecoveryActionViewModel);
    }

    public static void initFactory(Object mFlashlightObject,
                                   Map<String, Drawable> actionIcons,
                                   Map<String, String> actionStrings,
                                   Map<String, Object> actionViewModelDefaults,
                                   boolean prefUnlockKeyguardBeforeActionExecute) {

        FirefdsKitActionViewModelsFactory.mFlashlightObject = mFlashlightObject;
        FirefdsKitActionViewModelsFactory.actionIcons = actionIcons;
        FirefdsKitActionViewModelsFactory.actionStrings = actionStrings;
        FirefdsKitActionViewModelsFactory.actionViewModelDefaults = actionViewModelDefaults;
        FirefdsKitActionViewModelsFactory.prefUnlockKeyguardBeforeActionExecute = prefUnlockKeyguardBeforeActionExecute;
    }

    public static FirefdsKitActionViewModel getActionViewModel(String action) {
        Supplier<FirefdsKitActionViewModel> actionViewModelSupplier = actionViewModelMap.get(action);
        if (actionViewModelSupplier != null) {
            return actionViewModelSupplier.get();
        }
        return null;
    }
}
