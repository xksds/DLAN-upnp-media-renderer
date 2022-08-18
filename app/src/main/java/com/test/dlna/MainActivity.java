package com.test.dlna;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.dlna.service.DLNAServer;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DLNAServer mDLNAServer;
    private Button start_dlna_btn, stop_dlna_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_dlna_btn = (Button) findViewById(R.id.start_dlna_btn);
        stop_dlna_btn = (Button) findViewById(R.id.stop_dlna_btn);

        start_dlna_btn.setOnClickListener(this);
        stop_dlna_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_dlna_btn:
                start();
                break;
            case R.id.stop_dlna_btn:
                stop();
                break;
            default:
                break;
        }
    }

    private void start() {
        Log.d(TAG, "startDLNAServer ...");

        if (null == mDLNAServer) {
            mDLNAServer = new DLNAServer();
        }
        mDLNAServer.bindService();
    }

    private void stop() {
        Log.d(TAG, "stopDLNAServer ...");

        if (null != mDLNAServer) {
            mDLNAServer.unbindService();
            mDLNAServer = null;
        }
        return;
    }
}
