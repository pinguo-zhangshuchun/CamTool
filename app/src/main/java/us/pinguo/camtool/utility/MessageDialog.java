package us.pinguo.camtool.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by ws-kari on 15-8-19.
 */
public class MessageDialog {
    public static void exit(final Context context, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Errors occurred");
        builder.setMessage(message + "\nI will Exit.");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Activity activity = (Activity) context;
                activity.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void info(final Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Notice");
        builder.setMessage(message);
        builder.setPositiveButton("Close", null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

}
