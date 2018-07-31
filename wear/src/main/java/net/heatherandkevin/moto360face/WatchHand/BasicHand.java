package net.heatherandkevin.moto360face.WatchHand;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BasicHand extends WatchHand {

    private float length;
    private Paint handPaint;

    private static final int HAND_COLOR =  Color.rgb(183, 81, 12);
    private static final float HAND_STROKE_WIDTH = 3f;

    public BasicHand(float xCenter, float yCenter, float length) {
        super(xCenter, yCenter);
        this.length = length;

        handPaint = new Paint();
        handPaint.setColor(HAND_COLOR);
        handPaint.setAntiAlias(true);
        handPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        handPaint.setStrokeWidth(HAND_STROKE_WIDTH);
    }


    @Override
    public void draw(Canvas canvas, float rotation) {
        canvas.save();
        canvas.rotate(rotation, xCenter, yCenter);
        canvas.drawLine(xCenter, yCenter,
                xCenter, yCenter + length - 2 * HAND_STROKE_WIDTH,
                handPaint);
        canvas.restore();

        canvas.drawCircle(xCenter, yCenter,
                2, handPaint);
    }
}
