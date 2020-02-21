package sb.firefds.pie.firefdskit.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

import sb.firefds.pie.firefdskit.FirefdsKitActivity;
import sb.firefds.pie.firefdskit.R;
import sb.firefds.pie.firefdskit.utils.Constants;
import sb.firefds.pie.firefdskit.utils.Utils;

public class SaveDialog {

    private AlertDialog dialog;

    public SaveDialog() {
    }

    public void showDialog(Context context, View contentView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(context);
        editText.setHint(R.string.backup_name);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialog = builder.setCancelable(true).setTitle(R.string.save).setView(editText)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    if (savePreferencesToSdCard(context, editText.getText().toString())) {
                        Utils.createSnackbar(contentView,
                                R.string.save_successful,
                                context).show();
                    } else {
                        Utils.createSnackbar(contentView,
                                R.string.save_unsuccessful,
                                context).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create();
        dialog.show();
        dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean savePreferencesToSdCard(Context context, String string) {
        File dir = context.getExternalFilesDir(Constants.BACKUP_DIR);
        Objects.requireNonNull(dir).mkdirs();

        File file = new File(dir, string + ".fk");

        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(file));
            output.writeObject(FirefdsKitActivity.getSharedPreferences().getAll());

            res = true;
        } catch (IOException e) {
            Utils.log(e);
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                Utils.log(ex);
            }
        }
        return res;
    }
}
