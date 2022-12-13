package sb.firefds.q.firefdskit.features;

import de.robv.android.xposed.XSharedPreferences;
import sb.firefds.q.firefdskit.FeatureFactory;

/**
 * A feature is the implementation of an modification you want to do with Xposed.
 * <p>
 * Every class that implements {@link Feature} should also implement a static method
 * that determines if a feature is supported on which platforms. The static method
 * should have a signature like this:
 * <p>
 * <code>static boolean hasFeature(String featureName);</code>
 * </p>
 * This method should be used in the {@link FeatureFactory} to
 * actually create the feature if available.
 */
public interface Feature {
    void inject(ClassLoader param, XSharedPreferences pref, Utils utils);
}
