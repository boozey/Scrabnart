package com.nakedape.scrabnart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Nathan on 5/29/2015.
 */
public class DrawingView  extends View {
    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF000000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //draw events array
    private ArrayList<DrawEvent> drawEvents;

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
        drawEvents = new ArrayList<>(10);
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
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                drawPath.lineTo(touchX + 1, touchY + 1);
                drawEvents.add(new DrawEvent(DrawEvent.EVENT_START,
                        touchX, touchY,
                        drawPaint.getColor(),
                        drawPaint.getStrokeWidth(),
                        getHeight(), getWidth()));
                drawEvents.add(new DrawEvent(DrawEvent.EVENT_MOVE,
                        touchX + 1, touchY + 1,
                        drawPaint.getColor(),
                        drawPaint.getStrokeWidth(),
                        getHeight(), getWidth()));
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                drawEvents.add(new DrawEvent(DrawEvent.EVENT_MOVE,
                        touchX, touchY,
                        drawPaint.getColor(),
                        drawPaint.getStrokeWidth(),
                        getHeight(), getWidth()));
                break;
            case MotionEvent.ACTION_UP:
                drawEvents.add(new DrawEvent(DrawEvent.EVENT_END,
                        touchX, touchY,
                        drawPaint.getColor(),
                        drawPaint.getStrokeWidth(),
                        getHeight(), getWidth()));
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public ArrayList<DrawEvent> getDrawing(){
        return drawEvents;
    }
    public void setColor(int color){
        paintColor = color;
        drawPaint.setColor(paintColor);
    }
    public void setBrushSize(float width){
       drawPaint.setStrokeWidth(width);
    }
}
