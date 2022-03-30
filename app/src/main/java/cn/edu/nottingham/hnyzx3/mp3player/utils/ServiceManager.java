package cn.edu.nottingham.hnyzx3.mp3player.utils;

import android.app.ActivityManager;
import android.content.Context;

public class ServiceManager {

    /**
     * check if the specified service is running
     * @param context
     * @param serviceClass
     * @return
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
