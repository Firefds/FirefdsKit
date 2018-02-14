/*
 * Copyright (C) 2016 Mohamed Karami for XTouchWiz Project (Wanam@xda)
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
package sb.firefds.oreo.firefdskit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import sb.firefds.oreo.firefdskit.R;

public class WanamToastReceiver extends BroadcastReceiver {

	private static final String SHOW_TOAST = "ma.wanam.xposed.action.SHOW_TOAST";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equalsIgnoreCase(SHOW_TOAST)) {
			Toast.makeText(context,
					context.getResources().getString(R.string.killed_) + intent.getStringExtra("processName"),
					Toast.LENGTH_SHORT).show();

		}
	}
}
