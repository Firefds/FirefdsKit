package sb.firefds.q.firefdskit.features;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static sb.firefds.q.firefdskit.utils.Preferences.PREF_LOCKSCREEN_DOUBLE_TAP;

public class DoubleTapStatusBarOrLockScreenSdk31AndHigher implements Feature {

    protected Object mNotificationPanelViewController;
    protected GestureDetector mDoubleTapGesture;
    protected int mStatusBarHeight = 0;
    protected PowerManager mPowerManager;
    private int mStatusBarHeaderHeight = 0;
    private long mLastDownEvent = 0L;

    public static boolean isPlatformSupported(final String featureName) {
        return Build.VERSION.SDK_INT >= 31
                && (Objects.equals(featureName, PREF_LOCKSCREEN_DOUBLE_TAP));
    }

    @Override
    public void inject(final ClassLoader classLoader,
                       final XSharedPreferences pref,
                       final Utils utils) {
        final String notificationPanelViewController = utils.getComAndroidSystemui_NotificationPanelViewControllerClassName();
        findAndHookMethod(notificationPanelViewController, classLoader, "onFinishInflate", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (mPowerManager == null)
                    mPowerManager = (PowerManager) getObjectField(param.thisObject, "mPowerManager");
                mStatusBarHeaderHeight = getIntField(param.thisObject, "mStatusBarHeaderHeightKeyguard");
                final View view = (View) getObjectField(param.thisObject, "mView");
                registerGestureDetectorListener(param, view.getContext(), mPowerManager);
            }
        });

        String touchHandler = utils.isSecurityPatchAfterDecember2022() ? "com.android.systemui.shade.PanelViewController$TouchHandler" : "com.android.systemui.statusbar.phone.PanelViewController$TouchHandler";
        findAndHookMethod(touchHandler, classLoader, "onTouch", View.class, MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                final String notificationPanelView = utils.isSecurityPatchAfterDecember2022() ? "com.android.systemui.shade.NotificationPanelView" : "com.android.systemui.statusbar.phone.NotificationPanelView";
                if (param.args[0].getClass().getName().equals(notificationPanelView)) {
                    fireOnTouchEventIfPossible((MotionEvent) param.args[1], pref);
                }
            }
        });

        if (pref.getBoolean(PREF_LOCKSCREEN_DOUBLE_TAP, false)) {
            findAndHookMethod("com.android.systemui.statusbar.DragDownHelper", classLoader, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    MotionEvent event = (MotionEvent) param.args[0];
                    long time = event.getEventTime();
                    View host = (View) getObjectField(param.thisObject, "host");
                    if (mPowerManager == null)
                        mPowerManager = (PowerManager) host.getContext().getSystemService(Context.POWER_SERVICE);
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                            && event.getY() < mStatusBarHeaderHeight) {
                        if (time - mLastDownEvent < 300) {
                            callMethod(mPowerManager, "goToSleep", time);
                        }
                        mLastDownEvent = event.getEventTime();
                    }
                }
            });
        }
    }

    protected void registerGestureDetectorListener(final XC_MethodHook.MethodHookParam param,
                                                   final Context context,
                                                   final PowerManager powerManager) {
        mNotificationPanelViewController = param.thisObject;
        mStatusBarHeight = getIntField(param.thisObject, "mStatusBarMinHeight");

        if (mDoubleTapGesture == null) {
            mDoubleTapGesture = new GestureDetector(context,
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            callMethod(powerManager, "goToSleep", e.getEventTime());
                            return true;
                        }
                    });
        }
    }

    protected void fireOnTouchEventIfPossible(final MotionEvent event,
                                              final XSharedPreferences pref) {
        if (mNotificationPanelViewController != null && mDoubleTapGesture != null) {
            boolean isExpanded = getBooleanField(mNotificationPanelViewController, "mQsExpanded");
            boolean isPulsing = getBooleanField(mNotificationPanelViewController, "mPulsing");
            boolean isDozing = getBooleanField(mNotificationPanelViewController, "mDozing");
            boolean isKeyguard = getIntField(mNotificationPanelViewController, "mBarState") == 1
                    && !isPulsing && !isDozing;
            //boolean isStatusBar = event.getY() < mStatusBarHeight && !isExpanded;

            if (isKeyguard && pref.getBoolean(PREF_LOCKSCREEN_DOUBLE_TAP, false))
                mDoubleTapGesture.onTouchEvent(event);
        }
    }
}
