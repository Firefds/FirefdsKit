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
package sb.firefds.xtouchwizS5.dialogs;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import sb.firefds.xtouchwizS5.R;
import sb.firefds.xtouchwizS5.adapters.BackupAdapter;
import sb.firefds.xtouchwizS5.utils.Constants;
import android.widget.TextView;

public class RestoreDialog extends DialogFragment {

	private String path;
	private File dir;
	private File[] backups;
	private AlertDialog dialog;
	private RestoreDialogListener listener;
	private BackupAdapter adapter;
	private ListView listView;

	public RestoreDialog() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.listener = (RestoreDialogListener) activity;
	}

	public void restoreDefault() {
		listener.onRestoreDefaults();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BACKUP_DIR;
		LinearLayout linearLayout = new LinearLayout(getActivity());
		listView = new ListView(getActivity());
		linearLayout.addView(listView);
		TextView emptyView = new TextView(getActivity(), null, android.R.layout.simple_list_item_1);
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (Resources.getSystem()
				.getDisplayMetrics().density * 48)));
		emptyView.setGravity(Gravity.CENTER);
		linearLayout.addView(emptyView);
		emptyView.setText(R.string.no_backups);
		listView.setEmptyView(emptyView);
		dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		setupData();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				listener.onRestoreBackup(backups[arg2]);
				dialog.dismiss();
			}

		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
				PopupMenu menu = new PopupMenu(getActivity(), arg1);
				menu.inflate(R.menu.backup_item);
				menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if (item.getItemId() == R.id.action_delete) {
							backups[arg2].delete();
							setupData();
						}
						return true;
					}
				});
				menu.show();
				return true;
			}
		});
		dialog = builder.setCancelable(true).setTitle(R.string.restore).setView(linearLayout)
				.setPositiveButton(R.string.defaults, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener.onRestoreDefaults();
					}
				}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		return dialog;

	}

	private boolean setupData() {
		backups = dir.listFiles();
		if (backups == null || backups.length == 0) {
			return false;
		}
		adapter = new BackupAdapter(getActivity(), backups);
		listView.setAdapter(adapter);
		return true;
	}

	public interface RestoreDialogListener {
		public void onRestoreDefaults();

		public void onRestoreBackup(File file);
	}

}
