package com.ideabytes.scioty.scan;

/**
 * Created by ideabytes on 3/3/16.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ideabytes.scioty.user.WelcomeActivity;
import com.ideabytes.scioty.utility.Constants;
import com.ideabytes.scioty.R;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


public class ScannerActivity extends Activity implements
        Camera.PreviewCallback{
    /**
     * @author Suman
     * @since 2.0
     */
    private static final String TAG = "ScannerActivity";
    private CameraPreview mPreview;
    private Camera mCamera;
    private ImageScanner mScanner;
    private Handler mAutoFocusHandler;
    private boolean mPreviewing = true;
    private ToggleButton flashLight;
    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isCameraAvailable()) {
            // Cancel request if there is no rear-facing camera.
            cancelRequest();
            return;
        }

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAutoFocusHandler = new Handler();

        // Create and configure the ImageScanner;
        setupScanner();

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        mPreview = new CameraPreview(this, this, autoFocusCB);
        setContentView(R.layout.bar_code_scancamera);
        flashLight = (ToggleButton) findViewById(R.id.flshaLight);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        //
        ImageView close = (ImageView) findViewById(R.id.dialog2remove);
        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent(ScannerActivity.this,WelcomeActivity.class);
                startActivity(i);
                finish();
            }
        });

        TextView outputText = (TextView) findViewById(R.id.terminalOutput);
        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
        mAnimation.setDuration(7000);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        outputText.setAnimation(mAnimation);

    }

    public void setupScanner() {
        try {
            mScanner = new ImageScanner();
            mScanner.setConfig(0, Config.X_DENSITY, 3);
            mScanner.setConfig(0, Config.Y_DENSITY, 3);

            int[] symbols = getIntent().getIntArrayExtra(Constants.SCAN_MODES);
            if (symbols != null) {
                mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
                for (int symbol : symbols) {
                    mScanner.setConfig(symbol, Config.ENABLE, 1);
                }
            }
        } catch(Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Open the default i.e. the first rear facing camera.
            mCamera = Camera.open();
            if (mCamera == null) {
                // Cancel request if mCamera is null.
                cancelRequest();
                return;
            }

            mPreview.setCamera(mCamera);
            mPreview.showSurfaceView();
            mPreviewing = true;
        } catch(Exception e) {
            e.getMessage();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            // mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();

            // According to Jason Kuang on
            // http://stackoverflow.com/questions/6519120/how-to-recover-camera-preview-from-sleep,
            // there might be surface recreation problems when the device goes
            // to sleep. So lets just hide it and
            // recreate on resume
            mPreview.hideSurfaceView();

            mPreviewing = false;
            mCamera = null;
        }
    }

    public boolean isCameraAvailable() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void cancelRequest() {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(Constants.ERROR_INFO, "Camera unavailable");
        setResult(Activity.RESULT_CANCELED, dataIntent);
        finish();
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();

        Image barcode = new Image(size.width, size.height, "Y800");
        barcode.setData(data);

        int result = mScanner.scanImage(barcode);

        if (result != 0) {
            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mPreviewing = false;
            SymbolSet syms = mScanner.getResults();
            for (Symbol sym : syms) {
                String symData = sym.getData();
                Log.i(TAG, "scan code result=" + symData + " Scan type =" + sym.getType());
                if (!TextUtils.isEmpty(symData)) {
                    Intent dataIntent = new Intent();
                    dataIntent.putExtra(Constants.SCAN_RESULT, symData);
                    dataIntent.putExtra(Constants.SCAN_RESULT_TYPE, sym.getType());
                    setResult(Activity.RESULT_OK, dataIntent);
                    finish();
                    break;
                }
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null && mPreviewing) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    public void onToggleClicked(View view) {
        try {
            boolean hasFlash = this.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA_FLASH);
            if (hasFlash) {
                boolean on = ((ToggleButton) view).isChecked();
                if (on) {
                    // Log.i(TAG, "Button1 is on!");
                    Parameters p = mCamera.getParameters();
                    p.setFlashMode(Parameters.FLASH_MODE_TORCH);// flash ON
                    mCamera.setParameters(p);
                } else {
                    // Log.i(TAG, "Button1 is off!");
                    Parameters p = mCamera.getParameters();
                    p.setFlashMode(Parameters.FLASH_MODE_OFF);// flash OFF
                    mCamera.setParameters(p);
                }
            } else {
                new Utils(ScannerActivity.this).showToastMessage("No Flash light");
                //			Toast.makeText(getApplicationContext(),
                //					"Your Device Don't Have Flash Light", Toast.LENGTH_LONG)
                //					.show();
            }
        } catch(Exception e) {
            e.getMessage();
        }
    }

    public void onBackPressed() {
        Intent i = new Intent(ScannerActivity.this,WelcomeActivity.class);
        startActivity(i);
        finish();
    }


}
