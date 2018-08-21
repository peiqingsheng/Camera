package com.google.android.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import android.widget.Toast;

import com.google.android.entity.CameraParam;
import com.google.android.view.CameraView;
import com.google.android.view.CaptureLayout;
import com.google.android.view.R;
import com.google.android.listener.CaptureListener;
import com.google.android.listener.TypeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * This demo app saves the taken picture to a constant file.
 * $ adb pull /sdcard/Android/data/com.google.android.cameraview.demo/files/Pictures/picture.jpg
 */
public class TakePhotoActivity extends Activity implements View.OnClickListener {

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };
    private int mCurrentFlash;

    private CameraView mCameraView;
    private ImageView iv_photo, iv_back, iv_flash, iv_switch;
    private CaptureLayout mCaptureLayout;

    private Handler mBackgroundHandler;

    private Bitmap mBitmap;
    private CameraParam photoParam;
    private String picPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_takephoto);
            setParams();
            mCameraView = (CameraView) findViewById(R.id.camera);
            mCameraView.addCallback(mCallback);
            mCameraView.setPictureHeight((int) photoParam.getPicHight());

            iv_photo = (ImageView) findViewById(R.id.iv_photo);
            iv_back = (ImageView) findViewById(R.id.iv_back);
            iv_flash = (ImageView) findViewById(R.id.iv_flash);
            iv_switch = (ImageView) findViewById(R.id.iv_switch);
            mCaptureLayout = (CaptureLayout) findViewById(R.id.capture_layout);
            ArrayList<View> views = new ArrayList<>();
            views.add(iv_flash);
            views.add(iv_switch);
            mCameraView.setViewsRotation(views);

            iv_back.setOnClickListener(this);
            iv_flash.setOnClickListener(this);
            iv_switch.setOnClickListener(this);

            mCaptureLayout.setCaptureLisenter(new CaptureListener() {
                @Override
                public void takePictures() {
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                }
            });
            //确认 取消
            mCaptureLayout.setTypeLisenter(new TypeListener() {
                @Override
                public void cancel() {
                    mCaptureLayout.resetCaptureLayout();
                    iv_photo.setVisibility(View.INVISIBLE);
                    mBitmap = null;
                }

                @Override
                public void confirm() {
                    if (mBitmap == null) {
                        return;
                    }
                    mCaptureLayout.setVisibility(View.GONE);
                    save();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(TakePhotoActivity.this, "打开相机失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private void setParams() {
        photoParam = (CameraParam) getIntent().getSerializableExtra("CameraParam");
        if (photoParam == null) {
            //没有设置，缺省值
            photoParam = new CameraParam();
        }
        if (TextUtils.isEmpty(photoParam.getPicPath())) {
            photoParam.setPicPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            mCameraView.start();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(TakePhotoActivity.this, "打开相机失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        try {
            mCameraView.stop();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(TakePhotoActivity.this, "打开相机失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
        if (mBitmap != null) {
            mBitmap = null;
        }
    }


    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            System.gc();
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                        mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                        mBitmap = crop(mBitmap);
                        if (!TextUtils.isEmpty(photoParam.getWatermark())) {
                            mBitmap = addWatermark(mBitmap, photoParam.getWatermark());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_photo.setImageBitmap(mBitmap);
                                iv_photo.setVisibility(View.VISIBLE);
                                mCaptureLayout.startAlphaAnimation();
                                mCaptureLayout.startTypeBtnAnimator();
                            }
                        });
                    } catch (final Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TakePhotoActivity.this, "拍照失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });
                    }
                }
            });

        }
    };

    private Bitmap crop(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        float scaleW = photoParam.getPicHight() / bitmap.getWidth();
        float scaleH = photoParam.getPicHight() / bitmap.getHeight();
        float scale = scaleW > scaleH ? scaleH : scaleW;
        //对于尺寸和所给一样，也未旋转的，不能进行添加水印，变大一点点
        scale = scale == 1 ? 1.0001f : scale;
        matrix.postScale(scale, scale);
        float degrees = 0f;
        // 根据拍摄的方向旋转图像（纵向拍摄时要需要将图像旋转90度)
        if (mCameraView.getFacing() == CameraView.FACING_BACK) {
            degrees = (mCameraView.getCurrentOrientation()) * 90;
            //符合16:9的照片尺寸在9:16的情况下顺时针旋转90
            if (bitmap.getWidth() > bitmap.getHeight()) {
                degrees += 90;
            }
        }
        //前置摄像头为镜面显示，如果显示为原生就要相反
        if (mCameraView.getFacing() == CameraView.FACING_FRONT) {
            degrees = -(mCameraView.getCurrentOrientation()) * 90;
            //符合16:9的照片尺寸在9:16的情况下顺时针旋转90
            if (bitmap.getWidth() > bitmap.getHeight()) {
                degrees -= 90;
            }
        }

        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 添加水印
     */
    private Bitmap addWatermark(Bitmap cameraBitmap, String watermark) {
        int w = cameraBitmap.getWidth();
        int h = cameraBitmap.getHeight();
        Canvas mCanvas = new Canvas(cameraBitmap);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize((w > h ? w : h) / 40);
        StaticLayout layout = new StaticLayout(watermark, textPaint, w, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        mCanvas.translate(0, h - layout.getHeight());
        layout.draw(mCanvas);
        mCanvas.save(Canvas.ALL_SAVE_FLAG);
        mCanvas.restore();
        return cameraBitmap;
    }

    private void save() {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                File file = new File(photoParam.getPicPath(), photoParam.getPicPre() + System.currentTimeMillis() + ".jpg");
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    out.flush();
                    out.close();
                    picPath = file.getAbsolutePath();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
                } catch (FileNotFoundException e) {
                    Toast.makeText(TakePhotoActivity.this, "保存照片失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    Toast.makeText(TakePhotoActivity.this, "保存照片失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        try {
            int i = v.getId();
            if (i == R.id.iv_flash) {
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    iv_flash.setImageResource(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
            } else if (i == R.id.iv_switch) {
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
            } else if (i == R.id.iv_back) {
                onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(TakePhotoActivity.this, "打开相机失败，请检查设备和权限！" + getError(e), Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        getIntent().putExtra("result", picPath);
        setResult(RESULT_OK, getIntent());
        super.onBackPressed();
    }


    private String getError(Exception e) {
        return "(" + e.toString()+ ")";
    }
}
