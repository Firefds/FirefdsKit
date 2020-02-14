package sb.firefds.q.firefdskit.dialogs;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import sb.firefds.q.firefdskit.R;
import sb.firefds.q.firefdskit.utils.Utils;

public class CreditDialog {

    public CreditDialog() {
    }

    public AlertDialog.Builder getDialog(Context context) {
        PackageInfo pInfo;
        String pkgVersion = "";
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            pkgVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Utils.log(e);
        }
        TextView tv = new TextView(context);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(R.string.credit_details);
        tv.setPadding(16, 16, 16, 16);
        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.app_name) + " " + pkgVersion)
                .setView(tv)
                .setNeutralButton(R.string.ok_btn, (dialog, id) -> dialog.dismiss());
    }
}
