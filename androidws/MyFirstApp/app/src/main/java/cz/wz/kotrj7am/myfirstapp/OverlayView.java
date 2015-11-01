package cz.wz.kotrj7am.myfirstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Jerry on 6.10.2015.
 */

//public class OverlayView extends SurfaceView implements SurfaceHolder.Callback{
public class OverlayView extends View {
    public static final String TAG = "OverlayView";

    private SurfaceHolder mHolder;


    public OverlayView(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        /*
        mHolder = getHolder();
        mHolder.addCallback(this);
        /**/
    }

    private int start = 0;

    private int boxSize = 50;
    private int boxTop = 100;
    private int step = 1;

    //*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if((canvas == null) || (middle == null)){
            Log.d(TAG, "Canvas or middle coordinates are null.");
            return;
        }

        /*
        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(200, 0, 0));
        myPaint.setStrokeWidth(5);
        myPaint.setStyle(Paint.Style.STROKE);
        /**/

        //canvas.drawRect(start, boxTop, start + boxSize, boxSize + boxTop, myPaint);
        //canvas.drawRect(start, boxTop, start + boxSize, boxSize + boxTop, OverlayView.paint);
        drawRect(middle, boxSize, OverlayView.paint, canvas);

        /**/

        Log.d(TAG, "Canvas has " + canvas.getWidth() + " width and " + canvas.getHeight() + " height.");

        start += step;
       /* if(start + boxTop >= canvas.getWidth()){
            start = 0;
        }
/**/
        /*

        if(start++ < 30) {
            canvas.drawRect(100, 100, 200, 200, myPaint);
        } else if(start < 60){
            canvas.drawRect(10, 10, 100, 100, myPaint);
        } else {
            start = 0;
            canvas.drawRect(100, 100, 200, 200, myPaint);
        }
        /**/

        Log.d(TAG, "rectangle drawn for " + start);
    }

    private static Paint paint;

    static{
        paint = new Paint();
        paint.setColor(Color.rgb(200, 0, 0));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        Log.d(TAG, "Paint initialised.");
    }

    private CameraPreview.Coordinates middle;

    public void setMiddle(CameraPreview.Coordinates middle){
        this.middle = middle;
        Log.d(TAG, "Middle set to x=" + middle.x + ", y=" + middle.y + ", idx=" + middle.idx);
    }

    private double previewWidth = -1;
    private double previewHeight = -1;

    public void setPreviewSize(int width, int height){
        previewWidth = width;
        previewHeight = height;
    }

    //*
    public void drawRect(CameraPreview.Coordinates middle, int size, Paint paint, Canvas canvas){
        int sizeHalf = size/2;
        double x = middle.x;
        double y = middle.y;

        if(previewWidth > 0){
            x = canvas.getWidth() / previewWidth * x;
        }
        if(previewHeight > 0){
            y = canvas.getHeight() / previewHeight * y;
        }

        int ix = (int) x;
        int iy = (int) y;

        Log.d(TAG, "Rectangle drawn at X = " + ix + ", Y = " + iy);

        canvas.drawRect(ix - sizeHalf, iy - sizeHalf,
                ix + sizeHalf, iy + sizeHalf, paint);
    }
    /**/

    /*

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // This allows us to make our own draw calls to this canvas
        this.setWillNotDraw(false);


        Canvas canvas = mHolder.lockCanvas(null);
        //canvas.

        Paint myPaint = new Paint();
        myPaint.setColor(Color.rgb(0, 0, 0));
        myPaint.setStrokeWidth(5);
        myPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(100, 100, 200, 200, myPaint);

        Log.d(TAG, "rectangle drawn");

        mHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    /**/
}
