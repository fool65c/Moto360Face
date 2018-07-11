package net.heatherandkevin.moto360face;

import android.graphics.Canvas;
import android.graphics.Path;

public class DayFace extends AccentFace {

    private Path textCircle;

    private final float TEXT_SIZE = 12f;

    public DayFace(float xCenter, float yCenter, float radius,
                   float xCutOut, float yCutOut, float rCutOut) {
        super(xCenter, yCenter, radius, xCutOut, yCutOut, rCutOut);

        float textCircleRadius = radius - TEXT_SIZE / 2f;
        textCircle = new Path();
        textCircle.arcTo(xCenter - textCircleRadius, yCenter - textCircleRadius,
                xCenter + textCircleRadius, yCenter + textCircleRadius,
                127, 160, false);
        textCircle.close();

    }

    public void draw(Canvas canvas, int day, int dayNum) {
        super.draw(canvas);
        canvas.drawPath(textCircle, fill);
    }
}
