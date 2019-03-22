package de.robv.android.xposed.library.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

public class ListPreferenceFixedSummary extends ListPreference {
    public ListPreferenceFixedSummary(Context context) {
        super(context);
    }

    public ListPreferenceFixedSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        notifyChanged();
    }
}
