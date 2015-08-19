package us.pinguo.camtool.utility;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ws-kari on 15-8-19.
 */
public class Logger {
    private final static String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/camtool/log/";
    private final static String NAME = "debug.txt";

    public static void d(String tag, String msg) {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, NAME);
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            String now = format.format(new Date());
            FileWriter writer = new FileWriter(file, true);
            writer.write("\n" + now + "\n");
            writer.write(tag + ":" + msg + "\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
