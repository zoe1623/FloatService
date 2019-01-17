package com.zoe.floatservice.float_view;

import android.app.Activity;

public class SDKManager {

    public static void tryInitFloat(Activity activity) {
        Logger.e("SDKService tryInitFloat");
        FloatService.newInstance(activity);
    }

    public static void initFloatLocation(Activity activity, int x, int y) {
        FloatService.getInstance(activity).initLocation(x, y);
    }

    public static void tryDestroyFloat(Activity activity) {
        Logger.e("SDKService tryDestroyFloat");
        FloatService.getInstance(activity).onDestroy();
    }

    public static void tryHideFloat(Activity activity) {
        Logger.e("SDKService tryHideFloat");
        FloatService.getInstance(activity).hide();
    }

    public static void tryShowFloat(Activity activity) {
        Logger.e("SDKService tryShowFloat");
        FloatService.getInstance(activity).show();
    }

    public static void loginSuccess() {
    }
}
