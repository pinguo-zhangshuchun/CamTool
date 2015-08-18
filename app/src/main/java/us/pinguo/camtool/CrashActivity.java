package us.pinguo.camtool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


public class CrashActivity extends Activity {
    final static String TAG = "CamToolCrashActivity";
    private TextView mTvMessage;
    private Button mBtnRestart;
    private Button mBtnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        initView();
    }

    private void initView() {
        mTvMessage = (TextView) findViewById(R.id.crash_tv_msg);
        mTvMessage.setText("Crash !!!");
        mBtnExit = (Button) findViewById(R.id.crash_btn_exit);
        mBtnRestart = (Button) findViewById(R.id.crash_btn_restart);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        if (null != intent) {
           String msg =  intent.getExtras().getString("msg");
            mTvMessage.setText(msg);
        }
    }

}
