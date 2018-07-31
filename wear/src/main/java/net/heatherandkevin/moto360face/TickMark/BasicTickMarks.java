package net.heatherandkevin.moto360face.TickMark;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BasicTickMarks {
    private TickMark highlightTickMark;
    private TickMark basicTickMark;
    private float xCenter;
    private float yCenter;
    private float highlightMod;
    private float count;
    private float tickAngle;
    private float startAngle;
    private float endAngle;

    public BasicTickMarks(float xCenter, float yCenter, float tickTop,
                          float highlightMod, float count,
                          float startAngle, float endAngle) {
        final float HILIGHT_TICK_HEIGHT = 10;
        final float HILIGHT_TICK_WIDTH = 3f;
        final float BASIC_TICK_HEIGHT = 5;
        final float BASIC_TICK_WIDTH = 2;
        final int TICK_COLOR = Color.WHITE;

        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.highlightMod = highlightMod;
        this.startAngle = startAngle;
        this.endAngle = endAngle;

        this.setCount(count);

        Paint tickPaint = new Paint();
        tickPaint.setColor(TICK_COLOR);
        tickPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        tickPaint.setAntiAlias(true);

        highlightTickMark = new TickMark(HILIGHT_TICK_WIDTH, HILIGHT_TICK_HEIGHT,
                xCenter, tickTop, tickPaint);

        basicTickMark = new TickMark(BASIC_TICK_WIDTH, BASIC_TICK_HEIGHT,
                xCenter, tickTop, tickPaint);
    }

    public void setCount(float count) {
        if (this.count == count) {
            return;
        }

        this.count = count;
        tickAngle = ( endAngle - startAngle ) / count;

    }

    public float getTickAngle() {
        return tickAngle;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.rotate(startAngle, xCenter, yCenter);
        for(int currentTick=0; currentTick<=count; currentTick++) {
            if (currentTick % highlightMod == 0) {
                highlightTickMark.draw(canvas);
            } else {
                basicTickMark.draw(canvas);
            }
            canvas.rotate(tickAngle, xCenter, yCenter);

        }
        canvas.restore();
    }
}
