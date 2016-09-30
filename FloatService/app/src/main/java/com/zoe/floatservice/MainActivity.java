package com.zoe.floatservice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {
    private FloatService mFloat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_hide).setOnClickListener(this);
        findViewById(R.id.bt_show).setOnClickListener(this);
        mFloat = FloatService.getInstance(this);
        mFloat.onCreate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_hide:
                mFloat.hide();
                break;
            case R.id.bt_show:
                mFloat.show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFloat.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFloat.onDestroy();
    }
}
