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
            Coordinates brightest = findBrightestPixel(data, size.width, size.height);
            RGB rgb = getRgb(data, size.width, size.height, brightest);
            overlayView.setMiddle(brightest, rgb);
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
                int idx = y * width + x;
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

    private static RGB getRgb(byte[] fg, int width, int height, Coordinates co) throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length + " < minimum " + sz * 3 / 2);
        int Y, Cr = 0, Cb = 0;
        final int jDiv2 = co.y >> 1;
        Y = fg[co.idx];
        if (Y < 0){
            Y += 255;
        }
        final int cOff = sz + jDiv2 * width + (co.x >> 1) * 2;
        Cb = fg[cOff];
        if (Cb < 0)
            Cb += 127;
        else
            Cb -= 128;
        Cr = fg[cOff + 1];
        if (Cr < 0)
            Cr += 127;
        else
            Cr -= 128;

        int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
        if (R < 0)
            R = 0;
        else if (R > 255)
            R = 255;
        int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
        if (G < 0)
            G = 0;
        else if (G > 255)
            G = 255;
        int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
        if (B < 0)
            B = 0;
        else if (B > 255)
            B = 255;

        RGB rgb = new RGB();
        rgb.r = R;
        rgb.g = G;
        rgb.b = B;
        return rgb;
    }

    public static class HSV{
        public double h = 0;
        public double s = 0;
        public double v = 0;

        public static HSV convert(RGB rgb){
            HSV hsv = new HSV();
            int max = rgb.r, min = rgb.r;
            if (rgb.g > max) {
                max = rgb.g;
            }
            if (rgb.b > max) {
                max = rgb.b;
            }
            if (rgb.g < min) {
                min = rgb.g;
            }
            if (rgb.b < min) {
                min = rgb.b;
            }
            hsv.v = max/255.0;
            int delta = max - min;
            if (delta > 0.0) {
                // if delta is not 0 then max cannot be 0
                hsv.s = ((double)delta) / max;
                if (max == rgb.r) {
                    hsv.h = (double)(rgb.g - rgb.b) / delta;
                }
                else if (max == rgb.g) {
                    hsv.h = (rgb.b - rgb.r) / delta + 2;
                }
                else {
                    hsv.h = (rgb.r - rgb.g) / delta + 4;
                }
                hsv.h *= 60;
                if (hsv.h < 0.0) {
                    hsv.h += 360.0;
                }
            }
            else {
                hsv.s = 0.0;
                hsv.h = 0.0;
            }
            return hsv;
        }

        public RGB getCategory(){
            RGB rgb = new RGB();
            if(h < 30){
                // red
                rgb.setAll(255,  0,  0);
                rgb.name = "red";
            } else if(h < 90){
                // yellow
                rgb.setAll(255, 255, 0);
                rgb.name = "yellow";
            } else if(h < 150){
                // green
                rgb.setAll(0, 255, 0);
                rgb.name = "green";
            } else if(h < 210){
                // cyan
                rgb.setAll(0, 255, 255);
                rgb.name = "cyan";
            } else if(h < 270){
                // blue
                rgb.setAll(0, 0, 255);
                rgb.name = "blue";
            } else if(h < 330){
                // magenta
                rgb.setAll(255, 0, 255);
                rgb.name = "magenta";
            } else {
                // red
                rgb.setAll(255,  0,  0);
                rgb.name = "red";
            }
            return rgb;
        }
    }

    public static class Coordinates{
        public int x = 0;
        public int y = 0;
        public int idx = 0;
        public RGB rgb = new RGB();

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

    public static class RGB {
        public int r, g, b;
        public String name;

        public static RGB getColor(int clr){
            RGB rgb = new RGB();
            rgb.r =  clr & 0x000000ff;
            rgb.g = (clr & 0x0000ff00) >> 8;
            rgb.b = (clr & 0x00ff0000) >> 16;
            return rgb;
        }

        public boolean brighterThan(RGB other){
            int thiss = r + g + b;
            int otherr = other.r + other.g + other.b;
            boolean res = thiss > otherr;
            return res;
        }

        public void setAll(int r, int g, int b){
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
}
