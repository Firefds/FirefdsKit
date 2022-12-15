package sb.firefds.q.firefdskit;

import sb.firefds.q.firefdskit.features.DoubleTapStatusBarOrLockScreenSdk29;
import sb.firefds.q.firefdskit.features.Feature;

public class FeatureFactory {

    private FeatureFactory() {
    }

    /**
     * Create instances of features if available.
     *
     * @param featureName the feature you want to instantiate. See {@link sb.firefds.q.firefdskit.utils.Preferences}
     * @return null if feature is not available other the feature you want.
     */
    public static Feature createFeature(final String featureName) {
        Feature feature = null;

        if (DoubleTapStatusBarOrLockScreenSdk29.isPlatformSupported(featureName)) {
            feature = new DoubleTapStatusBarOrLockScreenSdk29();
        }
        return feature;
    }

    /**
     * Check if a feature is available on this android platform.
     *
     * @param featureName the feature you want to instantiate. See {@link sb.firefds.q.firefdskit.utils.Preferences}
     * @return true if available otherwise false
     */
    public static boolean hasFeature(final String featureName) {
        return DoubleTapStatusBarOrLockScreenSdk29.isPlatformSupported(featureName);
    }
}
