/*
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sb.firefds.xtouchwizS5.adapters;

import android.graphics.drawable.Drawable;

public class BasicIconListItem extends BasicListItem
                               implements IIconListAdapterItem {
    private Drawable mIconLeft;
    private Drawable mIconRight;

    public BasicIconListItem(String text, String subText, Drawable iconIdLeft, Drawable iconIdRight) {
        super(text, subText);

        mIconLeft = iconIdLeft;
        mIconRight = iconIdRight;
    }

    public BasicIconListItem(String text, String subText) {
        this(text, subText, null, null);
    }

    @Override
    public Drawable getIconLeft() {
        return mIconLeft;
    }

    @Override
    public Drawable getIconRight() {
        return mIconRight;
    }

    public void setIconIdLeft(Drawable icon) {
        mIconLeft = icon;		
    }

    public void setIconRight(Drawable icon) {
        mIconRight = icon;
    }

    public void setIconIds(Drawable left, Drawable right) {
        mIconLeft = left;
        mIconRight = right;
    }
}