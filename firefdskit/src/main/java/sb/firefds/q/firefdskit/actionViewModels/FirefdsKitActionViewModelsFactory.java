package sb.firefds.q.firefdskit.actionViewModels;

import androidx.core.util.Supplier;

import java.util.HashMap;
import java.util.Map;

import static sb.firefds.q.firefdskit.utils.Constants.DOWNLOAD_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.FLASHLIGHT_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.MULTIUSER_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.RECOVERY_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.RESTART_UI_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.SCREENSHOT_ACTION;
import static sb.firefds.q.firefdskit.utils.Constants.SCREEN_RECORD_ACTION;

public class FirefdsKitActionViewModelsFactory {

    private static Object mFlashlightObject;
    private static ActionIcons actionIcons;
    private static ActionStrings actionStrings;
    private static ActionViewModelDefaults actionViewModelDefaults;
    private static boolean prefUnlockKeyguardBeforeActionExecute;
    private static boolean prefReplaceRecoveryIcon;

    private static final Map<String, Supplier<FirefdsKitActionViewModel>> actionViewModelMap = new HashMap<>();

    private static final Supplier<FirefdsKitActionViewModel> flashLightActionViewModel = () ->
            new FlashLightActionViewModel(actionViewModelDefaults,
                    FLASHLIGHT_ACTION,
                    actionStrings.getFlashlight(),
                    null,
                    actionIcons.getFlashlight(),
                    mFlashlightObject,
                    actionStrings.getFlashlightOn(),
                    actionStrings.getFlashlightOff());

    private static final Supplier<FirefdsKitActionViewModel> screenRecordActionViewModel = () ->
            new ScreenRecordActionViewModel(actionViewModelDefaults,
                    SCREEN_RECORD_ACTION,
                    actionStrings.getScreenRecord(),
                    null,
                    actionIcons.getScreenRecord());

    private static final Supplier<FirefdsKitActionViewModel> restartSystemUiActionViewModel = () ->
            new RestartSystemUiActionViewModel(actionViewModelDefaults,
                    RESTART_UI_ACTION,
                    actionStrings.getRestartSystemUi(),
                    actionStrings.getRestartSystemUiConfirm(),
                    actionIcons.getRestartSystemUi());

    private static final Supplier<FirefdsKitActionViewModel> userSwitchActionViewModel = () ->
            new UserSwitchActionViewModel(actionViewModelDefaults,
                    MULTIUSER_ACTION,
                    actionStrings.getSwitchUser(),
                    null,
                    actionIcons.getSwitchUser());

    private static final Supplier<FirefdsKitActionViewModel> screenShotActionViewModel = () ->
            new ScreenShotActionViewModel(actionViewModelDefaults,
                    SCREENSHOT_ACTION,
                    actionStrings.getScreenshot(),
                    null,
                    actionIcons.getScreenshot());

    private static final Supplier<FirefdsKitActionViewModel> restartDownloadActionViewModel = () ->
            new RestartActionViewModel(actionViewModelDefaults,
                    DOWNLOAD_ACTION,
                    actionStrings.getDownload(),
                    actionStrings.getRebootConfirmDownload(),
                    actionIcons.getDownload(),
                    prefUnlockKeyguardBeforeActionExecute);

    private static final Supplier<FirefdsKitActionViewModel> restartRecoveryActionViewModel = () ->
            new RestartActionViewModel(actionViewModelDefaults,
                    RECOVERY_ACTION,
                    actionStrings.getRecovery(),
                    actionStrings.getRebootConfirmRecovery(),
                    prefReplaceRecoveryIcon ? actionIcons.getRestartStock() : actionIcons.getRecovery(),
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
                                   ActionIcons actionIcons,
                                   ActionStrings actionStrings,
                                   ActionViewModelDefaults actionViewModelDefaults,
                                   boolean prefUnlockKeyguardBeforeActionExecute,
                                   boolean prefReplaceRecoveryIcon) {

        FirefdsKitActionViewModelsFactory.mFlashlightObject = mFlashlightObject;
        FirefdsKitActionViewModelsFactory.actionIcons = actionIcons;
        FirefdsKitActionViewModelsFactory.actionStrings = actionStrings;
        FirefdsKitActionViewModelsFactory.actionViewModelDefaults = actionViewModelDefaults;
        FirefdsKitActionViewModelsFactory.prefUnlockKeyguardBeforeActionExecute = prefUnlockKeyguardBeforeActionExecute;
        FirefdsKitActionViewModelsFactory.prefReplaceRecoveryIcon = prefReplaceRecoveryIcon;
    }

    public static FirefdsKitActionViewModel getActionViewModel(String action) {
        Supplier<FirefdsKitActionViewModel> actionViewModelSupplier = actionViewModelMap.get(action);
        if (actionViewModelSupplier != null) {
            return actionViewModelSupplier.get();
        }
        return null;
    }
}
