package us.pinguo.camtool;

import android.app.Application;

/**
 * Created by ws-kari on 15-8-18.
 */
public class CrashApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
