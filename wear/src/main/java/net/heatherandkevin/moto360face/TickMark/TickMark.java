package net.heatherandkevin.moto360face.TickMark;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class TickMark {
    private Paint fillPaint;
    private Paint outlinePaint;

    private RectF tickMark;


    public TickMark(float width, float height,
                    float xCenter, float yTop,
                    Paint tickPaint) {
        this(width, height, xCenter, yTop, tickPaint, null);

    }
    public TickMark(float width, float height,
                    float xCenter, float yTop,
                    Paint fillPaint, Paint outlinePaint) {
        this.fillPaint = fillPaint;
        this.outlinePaint = outlinePaint;

        tickMark = new RectF(xCenter - width / 2, yTop,
                xCenter + width / 2, yTop - height);
    }

    public void draw(Canvas canvas) {
        if (fillPaint != null) {
            canvas.drawRect(tickMark, fillPaint);
        }

        if (outlinePaint != null) {
            canvas.drawRect(tickMark, outlinePaint);
        }
    }
}
