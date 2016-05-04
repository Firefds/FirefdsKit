/*
 * Copyright (C) 2011 The Android Open Source Project
 * Modifications Copyright (C) The OmniROM Project
 * Modifications Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Modifications Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
 *
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
 *
 * Per article 5 of the Apache 2.0 License, some modifications to this code
 * were made by the OmniROM Project.
 *
 * Modifications Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package sb.firefds.xtouchwizS5.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import sb.firefds.xtouchwizS5.services.ScreenRecordingService;
import sb.firefds.xtouchwizS5.utils.Packages;

public class ScreenRecordHandler implements InvocationHandler {
	private Context mContext;
	private String mScreenRecordLabel;
	private Drawable mScreenRecordIcon;

	public ScreenRecordHandler(Context context, String mScreenRecordLabel, Drawable mScreenRecordIcon) {
		this.mContext = context;
		this.mScreenRecordLabel = mScreenRecordLabel;
		this.mScreenRecordIcon = mScreenRecordIcon;
	}

	private void takeScreenrecord() {
		try {
			Context gbContext = mContext.createPackageContext(Packages.XTOUCHWIZ, Context.CONTEXT_IGNORE_SECURITY);
			Intent intent = new Intent(gbContext, ScreenRecordingService.class);
			intent.setAction(ScreenRecordingService.ACTION_SCREEN_RECORDING_START);
			gbContext.startService(intent);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String methodName = method.getName();

		if (methodName.equals("create")) {
			mContext = (Context) args[0];
			Resources res = mContext.getResources();
			LayoutInflater li = (LayoutInflater) args[3];
			int layoutId = res.getIdentifier("global_actions_item", "layout", "android");
			View v = li.inflate(layoutId, (ViewGroup) args[2], false);

			ImageView icon = (ImageView) v.findViewById(res.getIdentifier("icon", "id", "android"));
			icon.setImageDrawable(mScreenRecordIcon);

			TextView messageView = (TextView) v.findViewById(res.getIdentifier("message", "id", "android"));
			messageView.setText(mScreenRecordLabel);

			TextView statusView = (TextView) v.findViewById(res.getIdentifier("status", "id", "android"));
			statusView.setVisibility(View.GONE);

			return v;
		} else if (methodName.equals("onPress")) {
			takeScreenrecord();
			return null;
		} else if (methodName.equals("onLongPress")) {
			return true;
		} else if (methodName.equals("showDuringKeyguard")) {
			return true;
		} else if (methodName.equals("showBeforeProvisioning")) {
			return true;
		} else if (methodName.equals("isEnabled")) {
			return true;
		} else if (methodName.equals("showConditional")) {
			return true;
		} else {
			return null;
		}
	}
}
