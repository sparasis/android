package cz.wz.kotrj7am.myfirstapp;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Jerry on 2.10.2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private OverlayView overlayView;
    private CameraActivity activity;

    public CameraPreview(CameraActivity context, OverlayView overlayView) {
        super(context);
        activity = context;

        this.overlayView = overlayView;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
    }

    List<Camera.Size> mSupportedPreviewSizes;

    public void setCamera(Camera camera) {
        if (mCamera == camera) { return; }

        stopPreviewAndFreeCamera();

        mCamera = camera;

        if (mCamera != null) {
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            //requestLayout();
            mCamera.setPreviewCallback(this);

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.startPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //this.setWillNotDraw(false); // This allows us to make our own draw calls to this canvas}
    }

    private int cnt = 0;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        Surface surface = mHolder.getSurface();
        if ((surface == null) || (mCamera == null)){
            // preview surface or camera does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        int rotation = ((CameraActivity)getContext()).getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 90; break;
            case Surface.ROTATION_90: degrees = 0; break;
            case Surface.ROTATION_180: degrees = 0; break;
            case Surface.ROTATION_270: degrees = 180; break;
        }

        mCamera.setDisplayOrientation(degrees);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(getContext().getString(R.string.app_name), "Error starting camera preview: " + e.getMessage());
        }
    }

    //*
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        /*
        // stop preview after first frame
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }/**/

        /*
        Log.d("test", "got a frame, y=" +  data[100]);

        for(int i = 100; i < 200000; ++i) {
            data[i] = (byte) 0;
        }
        /**/

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();
        Log.d(TAG, "Preview has " + size.width + " width and " + size.height + " height. There are " + data.length + " bytes.");


        if (overlayView != null) {
            overlayView.setPreviewSize(size.width, size.height);
            overlayView.setMiddle(findBrightestPixel(data, size.width, size.height));
            overlayView.invalidate();
        }

        if (capture) {
            synchronized (lock) {
                if (capture) {

                    /*
                    // stop preview after first frame
                    try {
                        mCamera.stopPreview();
                    } catch (Exception e){
                        // ignore: tried to stop a non-existent preview
                    }
                    /**/
                    saveFile(data);
                    capture = false;
                }
            }
        }
    }

    private void saveFile(byte[] data){

        if(isExternalStorageWritable()){

            FileOutputStream outputStream;

            File dir = getAlbumStorageDir("testImages");
            int cnt = 0;
            File file;
            do{
                 file = new File(dir.getPath() + "/img" + cnt);
                ++cnt;
            } while (file.exists());

            try {
                outputStream = new FileOutputStream(file);
                //outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(data);
                outputStream.close();

                Context context = this.getContext();
                CharSequence text = "Captured image saved to\n" + file.getPath();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Log.d(TAG, "Captured image saved to " + file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }
    /**/

    public static Coordinates findBrightestPixel(byte[] data, int width, int height){
        int brightest = 0;
        Coordinates coordinates = new Coordinates();

        for(int y = 0; y < height; ++y){
            for(int x = 0; x < width; ++x){
                int idx = y * height + x;
                int current = data[idx];
                if(current < 0){
                    current += 256;
                }
                if(current > brightest){
                    brightest = current;
                    coordinates.setAll(x, y, idx);
                }
            }
        }

        Log.d(TAG, "Brightest pixel has value (byte)=" + data[coordinates.idx] + "(int)=" + brightest);

        return coordinates;
    }

    public static class Coordinates{
        public int x = 0;
        public int y = 0;
        public int idx = 0;

        public Coordinates(){
        }

        public Coordinates(int x, int y, int idx){
            setAll(x, y, idx);
        }

        public void setAll(int x, int y, int idx){
            this.x = x;
            this.y = y;
            this.idx = idx;
        }
    }

    private Object lock = new Object();
    private boolean stopped;

    public void startPreview(){
        synchronized(lock){
            mCamera.startPreview();
        }
    }

    private boolean capture = false;

    public void captureImage(){
        synchronized(lock){
            capture = true;
        }
    }
}
