package us.pinguo.camtool;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ws-kari on 15-8-18.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    final static String TAG = "CamToolCrashHandler";
    private Context mContext;
    private static CrashHandler sIntance = new CrashHandler();
    private final static String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/camtool/log/";
    private final static String NAME = getCurrentDateString() + ".txt";


    private CrashHandler() {
    }

    private static String getCurrentDateString() {
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault());
        Date nowDate = new Date();
        result = sdf.format(nowDate);
        return result;
    }

    public static CrashHandler getInstance() {
        return sIntance;
    }

    private String getCrashMessage(Throwable throwable) {
        String msg = null;
        ByteArrayOutputStream baos = null;
        PrintStream printStream = null;
        try {
            baos = new ByteArrayOutputStream();
            printStream = new PrintStream(baos);
            throwable.printStackTrace(printStream);
            byte[] data = baos.toByteArray();
            msg = new String(data);
            printStream.close();
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        handleException(throwable);
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);

        /*
        Intent intent = new Intent();
        intent.setClass(mContext, CrashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("msg", msg);
        mContext.startActivity(intent);
        */
    }

    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "CamTool crash!", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();

        String msg = getCrashMessage(ex);
        saveCrashInfo(mContext, msg);
        return true;
    }

    public void init(Context context) {
        mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    private void saveCrashInfo(Context context, String msg) {
        String pkgName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(pkgName, 0);
            File dir = new File(DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, NAME);
            FileWriter writer = new FileWriter(file, true);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            String now = format.format(new Date());
            writer.write("time:" + now + "\n");
            writer.write("package name:" + pkgName + "\n");
            writer.write("version code:" + packageInfo.versionCode + "\n");
            writer.write("model:" + Build.MODEL + "\n");
            writer.write("sdk version:" + Build.VERSION.SDK_INT + "\n");
            writer.write("release version:" + Build.VERSION.RELEASE + "\n");
            writer.write("error:" + msg + "\n");
            writer.write("\n\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
