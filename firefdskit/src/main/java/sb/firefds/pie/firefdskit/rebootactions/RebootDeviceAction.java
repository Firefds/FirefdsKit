package sb.firefds.pie.firefdskit.rebootactions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import sb.firefds.pie.firefdskit.R;

public abstract class RebootDeviceAction {

    private AppCompatActivity activity;

    RebootDeviceAction(AppCompatActivity activity) {
        this.activity = activity;
    }

    void showRebootDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(R.layout.progress_dialog).create().show();
    }

    public AppCompatActivity getActivity() {
        return activity;
    }
}
