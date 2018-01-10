package com.arcsoft.sdk_demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.soft.sdk_demo.R;

/**
 * Created by LL on 2018/1/10.
 */

public class WelAcitivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wel);
        Toast.makeText(this, "welcome!!!", Toast.LENGTH_SHORT).show();
    }
}
