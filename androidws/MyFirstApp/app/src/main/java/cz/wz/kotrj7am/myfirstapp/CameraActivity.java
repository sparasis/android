package cz.wz.kotrj7am.myfirstapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends AppCompatActivity {
    public static final String TAG = "CameraActivity";
    Camera mCamera = null;

    MyPreview mPreview;
    MyPreview2 mPreview2;
    CameraPreview cameraPreview;
    ModifiablePreview modifiablePreview;

    private boolean safeCameraOpen() {
        boolean qOpened = false;

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open();
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        cameraPreview.setCamera(null);
        if (mCamera != null) {
            //mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        /*
        // Create our Preview view and set it as the content of our activity.
        mPreview = new MyPreview(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        /**/

        /*
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview2 = new MyPreview2(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview2);
        /**/


        //*
        // Create our Preview view and set it as the content of our activity.
        OverlayView overlayView = new OverlayView(this);
        cameraPreview = new CameraPreview(this, overlayView);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        /**/

        preview.addView(overlayView);


        /*
        // Create our Preview view and set it as the content of our activity.
        modifiablePreview = new ModifiablePreview(this, null);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(modifiablePreview);
        /**/
    }

    @Override
     protected void onResume() {
        super.onResume();
        //*
       if(safeCameraOpen()){
           cameraPreview.setCamera(mCamera);
        }
        /**/
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LinearLayout layout = (LinearLayout)findViewById(R.id.camera_layout);
        int orientation = LinearLayout.VERTICAL;
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orientation = LinearLayout.HORIZONTAL;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            orientation = LinearLayout.VERTICAL;
        }
        layout.setOrientation(orientation);
    }

    public void capture(View view){
        //cameraPreview.startPreview();
        cameraPreview.captureImage();
    }

}
