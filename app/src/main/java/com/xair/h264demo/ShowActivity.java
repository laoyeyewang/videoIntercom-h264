package com.xair.h264demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

public class ShowActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wyav_home);
        getDefaultScreenDensity();
    }
    // 获取屏幕的默认分辨率
    public void getDefaultScreenDensity(){
        Display mDisplay = getWindowManager().getDefaultDisplay();
        int width = mDisplay.getWidth();
        int height = mDisplay.getHeight();
        Log.d("tag","Screen Default Ratio: ["+width+"x"+height+"]");
        Log.d("tag","Screen mDisplay: "+mDisplay);
    }
}
