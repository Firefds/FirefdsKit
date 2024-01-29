/*
 * Copyright (C) 2023 Shauli Bracha for Firefds Kit Project (Firefds@xda)
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
package sb.firefds.u.firefdskit.dialogs;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Objects;

import sb.firefds.u.firefdskit.R;
import sb.firefds.u.firefdskit.adapters.BackupAdapter;
import sb.firefds.u.firefdskit.utils.Constants;

public class RestoreDialog {

    private AlertDialog dialog;
    private File dir;
    private File[] backups;
    private ListView listView;
    private final RestoreDialogListener listener;

    public RestoreDialog(AppCompatActivity activity) {
        this.listener = (RestoreDialogListener) activity;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LinearLayout linearLayout = new LinearLayout(context);
        listView = new ListView(context);
        linearLayout.addView(listView);
        TextView emptyView = new TextView(context, null, android.R.layout.simple_list_item_1);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                             (int) (Resources.getSystem().getDisplayMetrics().density *
                                                                    48)));
        emptyView.setGravity(Gravity.CENTER);
        linearLayout.addView(emptyView);
        emptyView.setText(R.string.no_backups);
        listView.setEmptyView(emptyView);
        dir = context.getExternalFilesDir(Constants.BACKUP_DIR);
        if (!Objects.requireNonNull(dir).exists()) {
            dir.mkdirs();
        }

        setupData(context);

        listView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            listener.onRestoreBackup(backups[arg2]);
            dialog.dismiss();
        });
        listView.setOnItemLongClickListener((arg0, arg1, arg2, arg3) -> {
            PopupMenu menu = new PopupMenu(context, arg1);
            menu.inflate(R.menu.backup_item);
            menu.setOnMenuItemClickListener(item1 -> {
                if (item1.getItemId() == R.id.action_delete) {
                    backups[arg2].delete();
                    setupData(context);
                }
                return true;
            });
            menu.show();
            return true;
        });
        dialog = builder.setCancelable(true)
                        .setTitle(R.string.restore)
                        .setView(linearLayout)
                        .setPositiveButton(R.string.defaults, (dialog, which) -> listener.onRestoreDefaults())
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                        .create();
        dialog.show();
    }

    private void setupData(Context context) {
        backups = dir.listFiles();
        if (backups == null || backups.length == 0) {
            return;
        }
        BackupAdapter adapter = new BackupAdapter(context, backups);
        listView.setAdapter(adapter);
    }

    public interface RestoreDialogListener {
        void onRestoreDefaults();

        void onRestoreBackup(File file);
    }
}
