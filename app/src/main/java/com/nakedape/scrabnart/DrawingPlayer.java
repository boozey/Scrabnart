package com.nakedape.scrabnart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Nathan on 5/30/2015.
 */
public class DrawingPlayer extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //stroke array
    private ArrayList<DrawEvent> drawEvents;
    // Animation variables
    private long mAnimStartTime;
    private int eventIndex = 0;
    private Handler mHandler = new Handler();
    private Runnable mTick = new Runnable() {
        @Override
        public void run() {
            invalidate();
            if (eventIndex < drawEvents.size())
                mHandler.postDelayed(this, 20);
        }
    };

    public DrawingPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void SetData(ArrayList<DrawEvent> drawEvents){
        this.drawEvents = drawEvents;
    }

    public void DrawEvent(DrawEvent event){
        Log.d("Drawing", String.valueOf(event.getColor()) + " at " + String.valueOf(event.getX()) + ", " + String.valueOf(event.getY()));
            switch (event.getEventType()){
                case DrawEvent.EVENT_START:
                    drawPaint = new Paint();
                    drawPaint.setColor(event.getColor());
                    drawPaint.setAntiAlias(true);
                    drawPaint.setStrokeWidth(event.getBrushSize() * ((getWidth() + getHeight()) / 2));
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPaint.setStrokeJoin(Paint.Join.ROUND);
                    drawPaint.setStrokeCap(Paint.Cap.ROUND);
                    drawPath = new Path();
                    drawPath.moveTo(event.getX() * getWidth(), event.getY() * getHeight());
                    break;
                case DrawEvent.EVENT_MOVE:
                    drawPath.lineTo(event.getX() * getWidth(), event.getY() * getHeight());
                    break;
                case DrawEvent.EVENT_END:
                    drawCanvas.drawPath(drawPath, drawPaint);
                    break;
            }
    }

    public void Play(){
        mAnimStartTime = SystemClock.uptimeMillis();
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (drawEvents != null)
            if (eventIndex < drawEvents.size())
                DrawEvent(drawEvents.get(eventIndex++));
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }
}
