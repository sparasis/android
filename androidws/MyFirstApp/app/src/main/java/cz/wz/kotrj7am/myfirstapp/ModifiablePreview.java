package cz.wz.kotrj7am.myfirstapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jerry on 2.10.2015.
 */
public class ModifiablePreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String TAG = "ModifiablePreview";

    private int width;
    private int height;

    private SurfaceHolder mHolder;

    private Camera mCamera;
    private int[] rgbints;

    private boolean isPreviewRunning = false;

    private int mMultiplyColor;

    public ModifiablePreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mMultiplyColor = getResources().getColor(R.integer.multiply_color);
    }


    // @Override
    // protected void onDraw(Canvas canvas) {
    // Log.w(this.getClass().getName(), "On Draw Called");
    // }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        //*
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
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 0; break;
            case Surface.ROTATION_180: degrees = 0; break;
            case Surface.ROTATION_270: degrees = 0; break;
        }

        mCamera.setDisplayOrientation(degrees);
        Log.d(TAG, "rotation set to " + degrees);

        // start preview with new settings
        try {
            //mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(getContext().getString(R.string.app_name), "Error starting camera preview: " + e.getMessage());
        }
        /**/
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            if (isPreviewRunning)
                return;

            this.setWillNotDraw(false); // This allows us to make our own draw calls to this canvas


            mCamera = Camera.open();
            isPreviewRunning = true;
            Camera.Parameters p = mCamera.getParameters();
            Camera.Size size = p.getPreviewSize();
            width = size.width;
            height = size.height;
            Log.d(TAG, "camera preview width: " + width + ", height: " + height);
            p.setPreviewFormat(ImageFormat.NV21);
            //showSupportedCameraFormats(p);
            try {
                mCamera.setParameters(p);
                Log.d(TAG, "camera preview format set");
            } catch(RuntimeException ex){
                Log.e(TAG, "cannot set preview format");
            }

            rgbints = new int[width * height];

            // try { mCamera.setPreviewDisplay(holder); } catch (IOException e)
            // { Log.e("Camera", "mCamera.setPreviewDisplay(holder);"); }

            mCamera.startPreview();
            mCamera.setPreviewCallback(this);

        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            try {
                if (mCamera != null) {
                    //mHolder.removeCallback(this);
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    isPreviewRunning  = false;
                    mCamera.release();
                }
            } catch (Exception e) {
                Log.e("Camera", e.getMessage());
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // Log.d("Camera", "Got a camera frame");
        if (!isPreviewRunning)
            return;

        Canvas canvas = null;

        if (mHolder == null) {
            return;
        }

        mCamera.stopPreview();


        try {
            synchronized (mHolder) {
                canvas = mHolder.lockCanvas(null);
                int canvasWidth = canvas.getWidth();
                int canvasHeight = canvas.getHeight();

                decodeYUV(rgbints, data, width, height);
                //ModifiablePreview.YUV_NV21_TO_RGB(rgbints, data, width, height);

                // draw the decoded image, centered on canvas
                canvas.drawBitmap(rgbints, 0, width, canvasWidth-((width+canvasWidth)>>1),
                        canvasHeight-((height+canvasHeight)>>1), width, height, false, null);

                // use some color filter
                //canvas.drawColor(mMultiplyColor, PorterDuff.Mode.MULTIPLY);
                Paint myPaint = new Paint();
                myPaint.setColor(Color.rgb(200, 0, 0));
                myPaint.setStrokeWidth(5);
                myPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(100, 100, 200, 200, myPaint);

            }
        }  catch (Exception e){
            e.printStackTrace();
        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (canvas != null) {
                mHolder.unlockCanvasAndPost(canvas);
            }
        }

        mCamera.startPreview();
    }



    /**
     * Decodes YUV frame to a buffer which can be use to create a bitmap. use
     * this for OS < FROYO which has a native YUV decoder decode Y, U, and V
     * values on the YUV 420 buffer described as YCbCr_422_SP by Android
     *
     * @param out
     *            the outgoing array of RGB bytes
     * @param fg
     *            the incoming frame bytes
     * @param width
     *            of source frame
     * @param height
     *            of source frame
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public void decodeYUV(int[] out, byte[] fg, int width, int height) throws NullPointerException, IllegalArgumentException {
        int sz = width * height;
        if (out == null)
            throw new NullPointerException("buffer out is null");
        if (out.length < sz)
            throw new IllegalArgumentException("buffer out size " + out.length + " < minimum " + sz);
        if (fg == null)
            throw new NullPointerException("buffer 'fg' is null");
        if (fg.length < sz)
            throw new IllegalArgumentException("buffer fg size " + fg.length + " < minimum " + sz * 3 / 2);
        int i, j;
        int Y, Cr = 0, Cb = 0;
        for (j = 0; j < height; j++) {
            int pixPtr = j * width;
            final int jDiv2 = j >> 1;
            for (i = 0; i < width; i++) {
                Y = fg[pixPtr];
                if (Y < 0)
                    Y += 255;
                if ((i & 0x1) != 1) {
                    final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
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
                }
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
                out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
            }
        }

    }

    public static void YUV_NV21_TO_RGB(int[] argb, byte[] yuv, int width, int height) {
        final int frameSize = width * height;

        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
    }

    private void showSupportedCameraFormats(Camera.Parameters p) {
        List<Integer> supportedPictureFormats = p.getSupportedPreviewFormats();
        Log.d(TAG, "preview format:" + cameraFormatIntToString(p.getPreviewFormat()));
        for (Integer x : supportedPictureFormats) {
            Log.d(TAG, "supported format: " + cameraFormatIntToString(x.intValue()));
        }

    }

    private String cameraFormatIntToString(int format) {
        switch (format) {
            case PixelFormat.JPEG:
                return "JPEG";
            case PixelFormat.YCbCr_420_SP:
                return "NV21";
            case PixelFormat.YCbCr_422_I:
                return "YUY2";
            case PixelFormat.YCbCr_422_SP:
                return "NV16";
            case PixelFormat.RGB_565:
                return "RGB_565";
            default:
                return "Unknown:" + format;

        }
    }
}
