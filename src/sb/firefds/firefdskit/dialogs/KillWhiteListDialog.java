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
package sb.firefds.firefdskit.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import sb.firefds.firefdskit.MainApplication;
import sb.firefds.firefdskit.R;

public class KillWhiteListDialog extends DialogFragment {

	private AlertDialog dialog;
	private PackageManager packageManager;
	private List<ResolveInfo> appsList;
	private CharSequence[] appsLabelArray;
	private boolean[] appsCheckedArray;

	public KillWhiteListDialog() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
		launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		packageManager = getActivity().getPackageManager();
		appsList = packageManager.queryIntentActivities(launcherIntent, 0);
		Collections.sort(appsList, new ResolveInfo.DisplayNameComparator(packageManager));
		appsLabelArray = new CharSequence[appsList.size()];
		appsCheckedArray = new boolean[appsList.size()];
		String[] whiteArray = MainApplication.getSharedPreferences().getString("enableLongBackKillWhiteList", "")
				.split(";");
		List<String> whiteList = new ArrayList<String>(whiteArray.length);
		whiteList.addAll(Arrays.asList(whiteArray));
		for (int i = 0; i < appsList.size(); i++) {
			appsLabelArray[i] = appsList.get(i).loadLabel(packageManager);
			if (whiteList.contains(appsList.get(i).activityInfo.packageName)) {
				appsCheckedArray[i] = true;
			} else {
				appsCheckedArray[i] = false;
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		OnMultiChoiceClickListener listener = new OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				appsCheckedArray[which] = isChecked;
			}
		};
		dialog = builder.setMultiChoiceItems(appsLabelArray, appsCheckedArray, listener).setCancelable(true)
				.setTitle("Kill White List").setPositiveButton(R.string.apply, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						StringBuilder stringBuilder = new StringBuilder();
						for (int i = 0; i < appsCheckedArray.length; i++) {
							if (appsCheckedArray[i] == true) {
								stringBuilder.append(appsList.get(i).activityInfo.packageName + ";");
							}
						}

						MainApplication
								.getSharedPreferences()
								.edit()
								.putString(
										"enableLongBackKillWhiteList",
										((stringBuilder.length() > 1) ? stringBuilder.toString().substring(0,
												stringBuilder.length() - 1) : "")).commit();

					}
				}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();

		return dialog;
	}

}
