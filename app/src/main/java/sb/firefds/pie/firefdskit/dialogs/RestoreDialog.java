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
package sb.firefds.pie.firefdskit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;

import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.adapters.BackupAdapter;
import sb.firefds.pie.firefdskit.utils.Constants;

@SuppressWarnings("deprecation")
public class RestoreDialog extends DialogFragment {

    private File dir;
    private File[] backups;
    private AlertDialog dialog;
    private RestoreDialogListener listener;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + Constants.BACKUP_DIR;
        LinearLayout linearLayout = new LinearLayout(getActivity());
        listView = new ListView(getActivity());
        linearLayout.addView(listView);
        TextView emptyView = new TextView(getActivity(), null, android.R.layout.simple_list_item_1);
        emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                (int) (Resources.getSystem().getDisplayMetrics().density * 48)));
        emptyView.setGravity(Gravity.CENTER);
        linearLayout.addView(emptyView);
        emptyView.setText(R.string.no_backups);
        listView.setEmptyView(emptyView);
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        setupData();

        listView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            listener.onRestoreBackup(backups[arg2]);
            dialog.dismiss();
        });
        listView.setOnItemLongClickListener((arg0, arg1, arg2, arg3) -> {
            PopupMenu menu = new PopupMenu(getActivity(), arg1);
            menu.inflate(R.menu.backup_item);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    backups[arg2].delete();
                    setupData();
                }
                return true;
            });
            menu.show();
            return true;
        });
        dialog = builder.setCancelable(true).setTitle(R.string.restore).setView(linearLayout)
                .setPositiveButton(R.string.defaults, (dialog, which) -> listener.onRestoreDefaults())
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();
        return dialog;

    }

    private void setupData() {
        backups = dir.listFiles();
        if (backups == null || backups.length == 0) {
            return;
        }
        BackupAdapter adapter = new BackupAdapter(getActivity(), backups);
        listView.setAdapter(adapter);
    }

    public interface RestoreDialogListener {
        void onRestoreDefaults();
        void onRestoreBackup(File file);
    }
}
