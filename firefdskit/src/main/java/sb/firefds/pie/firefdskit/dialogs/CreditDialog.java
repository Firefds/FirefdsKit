package sb.firefds.pie.firefdskit.dialogs;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import de.robv.android.xposed.XposedBridge;
import sb.firefds.pie.firefdskit.R;

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
            XposedBridge.log(e);
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
