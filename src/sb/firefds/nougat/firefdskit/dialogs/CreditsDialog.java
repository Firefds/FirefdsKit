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
package sb.firefds.nougat.firefdskit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import sb.firefds.nougat.firefdskit.R;

public class CreditsDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		PackageInfo pInfo;
		String pkgVersion = "";
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			pkgVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		TextView tv = new TextView(getActivity());
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(R.string.credit_details);
		tv.setPadding(16, 16, 16, 16);
		builder.setCancelable(true).setView(tv).setTitle(getString(R.string.app_name) + " " + pkgVersion)
				.setNeutralButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		return builder.create();
	}
}
