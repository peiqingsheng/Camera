package com.example.pei.camera;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.activity.TakePhotoActivity;
import com.google.android.entity.CameraParam;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKEPHOTO = 1;
    private  final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_take).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TakePhotoActivity.class);
                CameraParam takePhotoParam = new CameraParam(CameraParam.Quantity.High);
                takePhotoParam.setWatermark(DateUtil.getCurDateStr(DateUtil.FORMAT_YMDHMS));
                intent.putExtra("CameraParam", takePhotoParam);
                startActivityForResult(intent, REQUEST_TAKEPHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) return;
        switch (requestCode) {
            case REQUEST_TAKEPHOTO:
                String picPath = data.getExtras().getString("result");
                if (!TextUtils.isEmpty(picPath)) {
                    //拍摄照片路径
                    Log.i(TAG,picPath);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
