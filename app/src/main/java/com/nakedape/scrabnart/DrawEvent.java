package com.nakedape.scrabnart;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Nathan on 5/30/2015.
 */
public class DrawEvent implements Serializable, Parcelable {

    final static int EVENT_START = 0;
    final static int EVENT_MOVE = 1;
    final static int EVENT_END = 2;

    private float x, y;
    private int color;
    private float brushSize;
    private int eventType;
    private float height, width;

    public DrawEvent(){}
    public DrawEvent(int event, float x, float y, int color, float brushSize, float height, float width){
        this.height = height;
        this.width = width;
        this.eventType = event;
        this.x = x / width;
        this.y = y /height;
        this.color = color;
        this.brushSize = brushSize / ((width + height) / 2);
    }

    public int getEventType() {return eventType;}
    public float getX() {return x;}
    public float getY() {return y;}
    public int getColor() {return color;}
    public float getBrushSize() {return brushSize;}

    // Parcelable Implementation
    public DrawEvent(Parcel in){
        float[] floats = new float[3];
        in.readFloatArray(floats);
        x = floats[0];
        y = floats[1];
        brushSize = floats[2];

        int[] ints = new int[2];
        in.readIntArray(ints);
        color = ints[0];
        eventType = ints[1];
    }
    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeFloatArray(new float[] {this.x, this.y, this.brushSize});
        dest.writeIntArray(new int[] {this.color, this.eventType});
    }
    transient public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DrawEvent createFromParcel(Parcel in) {
            return new DrawEvent(in);
        }

        public DrawEvent[] newArray(int size) {
            return new DrawEvent[size];
        }
    };
}
