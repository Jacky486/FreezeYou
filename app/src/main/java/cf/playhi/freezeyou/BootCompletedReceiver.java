package cf.playhi.freezeyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import net.grandcentrix.tray.AppPreferences;

import static cf.playhi.freezeyou.ApplicationIconUtils.getApplicationIcon;
import static cf.playhi.freezeyou.ApplicationIconUtils.getBitmapFromDrawable;
import static cf.playhi.freezeyou.NotificationUtils.createNotification;
import static cf.playhi.freezeyou.Support.checkMRootFrozen;
import static cf.playhi.freezeyou.Support.checkRootFrozen;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case Intent.ACTION_BOOT_COMPLETED:
                    runBackgroundService(context);
                    checkAndReNotifyNotifications(context);
                    checkTasks(context);
                    break;
                case Intent.ACTION_MY_PACKAGE_REPLACED:
                    runBackgroundService(context);
                    checkAndReNotifyNotifications(context);
                    checkTasks(context);
                    break;
                default:
                    break;
            }
        }
    }

    private void runBackgroundService(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("onekeyFreezeWhenLockScreen", false)) {
            ServiceUtils.startService(context, new Intent(context, ScreenLockOneKeyFreezeService.class));
        }
    }

    private void checkAndReNotifyNotifications(Context context) {
        AppPreferences defaultSharedPreferences = new AppPreferences(context);
        String string = defaultSharedPreferences.getString("notifying", "");
        if (string != null && !"".equals(string)) {
            String[] strings = string.split(",");
            PackageManager pm = context.getPackageManager();
            for (String aPkgName : strings) {
                if (!checkFrozenStatus(context, aPkgName, pm)) {
                    createNotification(context, aPkgName, R.drawable.ic_notification, getBitmapFromDrawable(getApplicationIcon(context, aPkgName, ApplicationInfoUtils.getApplicationInfoFromPkgName(aPkgName, context), false)));
                }
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String oldNotifying = sharedPreferences.getString("notifying", "");
        if (oldNotifying != null && !"".equals(oldNotifying)) {
            String[] oldNotifyings = oldNotifying.split(",");
            PackageManager pm = context.getPackageManager();
            for (String aPkgName : oldNotifyings) {
                if (!checkFrozenStatus(context, aPkgName, pm)) {
                    createNotification(
                            context,
                            aPkgName,
                            R.drawable.ic_notification,
                            getBitmapFromDrawable(
                                    getApplicationIcon(
                                            context,
                                            aPkgName,
                                            ApplicationInfoUtils
                                                    .getApplicationInfoFromPkgName(
                                                            aPkgName,
                                                            context
                                                    ),
                                            false)
                            )
                    );
                }
            }
            sharedPreferences.edit().putString("notifying", "").apply();
        }
    }

    private boolean checkFrozenStatus(Context context, String packageName, PackageManager pm) {
        return (checkRootFrozen(context, packageName, pm) || checkMRootFrozen(context, packageName));
    }

    private void checkTasks(Context context) {
        checkTimeTasks(context);
        checkTriggerTasks(context);
    }

    private void checkTimeTasks(Context context) {
        TasksUtils.checkTimeTasks(context);
    }

    private void checkTriggerTasks(Context context) {
        TasksUtils.checkTriggerTasks(context);
    }
}
