package sb.firefds.xtouchwizS5.receivers;

import android.content.Context;
import android.content.Intent;

public interface BroadcastSubReceiver {
	void onBroadcastReceived(Context context, Intent intent);
}
