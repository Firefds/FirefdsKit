package de.robv.android.xposed.library.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class SeparatorPreference extends Preference {
    int color = Color.GRAY;
    int height = 7;

    public SeparatorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSelectable(false);

        if (attrs != null) {
            height = attrs.getAttributeIntValue(null, "height", height);
        }
    }

    public SeparatorPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public SeparatorPreference(Context context) {
        this(context, null);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView iview = new ImageView(getContext());
        iview.setBackgroundColor(color);
        iview.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height));
        holder.setDividerAllowedBelow(true);
    }
}