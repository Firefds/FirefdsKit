package sb.firefds.q.firefdskit.features;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.view.MotionEvent;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import sb.firefds.q.firefdskit.utils.Preferences;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class DoubleTapStatusBarOrLockScreenSdk29 extends DoubleTapStatusBarOrLockScreenSdk31AndHigher {

    public static boolean isPlatformSupported(final String featureName) {
        return Build.VERSION.SDK_INT == 29 // Android 10
                && (Objects.equals(featureName, Preferences.PREF_LOCKSCREEN_DOUBLE_TAP));
    }

    @Override
    public void inject(final ClassLoader classLoader,
                       final XSharedPreferences pref,
                       final Utils utils) {
        findAndHookMethod("com.android.systemui.statusbar.phone.PanelView", classLoader, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                final Context context = (Context) getObjectField(param.thisObject, "mContext");
                if (mPowerManager == null)
                    mPowerManager = context.getSystemService(PowerManager.class);
                registerGestureDetectorListener(param, context, mPowerManager);
            }
        });

        findAndHookMethod("com.android.systemui.statusbar.phone.NotificationPanelView", classLoader, "onTouchEvent", MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                fireOnTouchEventIfPossible((MotionEvent) param.args[0], pref);
            }
        });
    }
}
