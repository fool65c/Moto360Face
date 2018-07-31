package net.heatherandkevin.moto360face.FaceAccents;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import net.heatherandkevin.moto360face.SubDial.RoundSubDial;
import net.heatherandkevin.moto360face.SubDial.SubDial;
import net.heatherandkevin.moto360face.TickMark.BasicTickMarks;
import net.heatherandkevin.moto360face.WatchHand.BasicHand;
import net.heatherandkevin.moto360face.WatchHand.WatchHand;

public class BatteryFace {
    private float xCenter;
    private float yCenter;
    private float radius;

    private Paint batteryLogoPaint;

    private BasicTickMarks tickMarks;
    private SubDial subDial;
    private WatchHand hand;

    private static final int BATERY_COLOR = Color.WHITE;

    public BatteryFace(float xCenter, float yCenter, float radius) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;

        batteryLogoPaint = new Paint();
        batteryLogoPaint.setColor(BATERY_COLOR);
        batteryLogoPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        batteryLogoPaint.setAntiAlias(true);

        tickMarks = new BasicTickMarks(xCenter, yCenter,
                yCenter + radius - 3, 5,
                20, 45, 315);

        subDial = new RoundSubDial(xCenter, yCenter, radius);
        hand = new BasicHand(xCenter, yCenter, radius);
    }

    public void draw(Canvas canvas, float batteryLevel) {
        //draw background
        subDial.draw(canvas);

        // tick marks
        tickMarks.draw(canvas);

        //draw hand
        hand.draw(canvas, 45 + 270 * batteryLevel);

        //draw battery logo
        canvas.drawRect(xCenter - 5, yCenter + radius - 20f,
                xCenter + 5, yCenter + radius -5,
                batteryLogoPaint);

        canvas.drawRect(xCenter - 3, yCenter + radius - 23f,
                xCenter + 3, yCenter + radius -20,
                batteryLogoPaint);
    }
}
