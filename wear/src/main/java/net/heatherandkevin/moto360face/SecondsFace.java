package net.heatherandkevin.moto360face;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;

class SecondsFace {
    private Paint fill;
    private Paint outline;
    private Paint tickPaint;
    private Paint secondHandPaint;
    private Paint secondHandKnobPaint;

    private float xCenter;
    private float yCenter;
    private float radius;
    private float innerTickRadius;
    private float outerTickRadius;

    private static final int SECONDHAND_COLOR =  Color.rgb(175, 96, 26);
    private static final int FILL_COLOR = Color.BLACK;
    private static final int FILL_ALPHA = 125;
    private static final int TICK_COLOR = Color.WHITE;
    private static final int GRADIENT_START_COLOR = Color.BLACK;
    private static final int GRADIENT_END_COLOR = Color.WHITE;

    private static final float STROKE_WIDTH = 2f;
    private static final float TICK_STROKE_WIDTH = 1.5f;
    private static final float SECONDHAND_STROKE_WIDTH = 3f;

    public SecondsFace(float xCenter, float yCenter, float radius) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;

        outerTickRadius= xCenter + radius - 3;

        fill = new Paint();
        fill.setColor(FILL_COLOR);
        fill.setAntiAlias(true);
        fill.setAlpha(FILL_ALPHA);
        fill.setStyle(Paint.Style.FILL);

        outline = new Paint();
        outline.setAntiAlias(true);
        outline.setStyle(Paint.Style.STROKE);
        outline.setStrokeWidth(STROKE_WIDTH);

        Shader gradientShader = new LinearGradient(
                xCenter + radius, yCenter,
                (int) xCenter + radius, (int) yCenter+ radius,
                GRADIENT_START_COLOR, GRADIENT_END_COLOR,
                Shader.TileMode.CLAMP);

        outline.setShader(gradientShader);

        tickPaint = new Paint();
        tickPaint.setColor(TICK_COLOR);
        tickPaint.setAntiAlias(true);
        tickPaint.setStyle(Paint.Style.STROKE);

        secondHandPaint = new Paint();
        secondHandPaint.setColor(SECONDHAND_COLOR);
        secondHandPaint.setAntiAlias(true);
        secondHandPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        secondHandPaint.setStrokeWidth(SECONDHAND_STROKE_WIDTH);

        secondHandKnobPaint = new Paint(secondHandPaint);

        int[] colors = {
                Color.rgb(237, 187, 153),
                SECONDHAND_COLOR,
                Color.rgb(237, 187, 153),
                SECONDHAND_COLOR,
                Color.rgb(237, 187, 153)
        };

        SweepGradient secondHandShader = new SweepGradient(xCenter, yCenter, colors, null);
        secondHandKnobPaint.setShader(secondHandShader);






    }

    public void draw (Canvas canvas, float seconds) {
        canvas.drawCircle(xCenter, yCenter,
                radius,
                outline);

        canvas.drawCircle(xCenter, yCenter,
                radius,
                fill);

        // tick marks
        canvas.save();
        final float tickRot = 6f;
        for (int tickIndex = 0; tickIndex < 60; tickIndex++) {


            if (tickIndex % 5 == 0) {
                tickPaint.setStrokeWidth(TICK_STROKE_WIDTH * 2);
                innerTickRadius = outerTickRadius - 10;
            } else {
                tickPaint.setStrokeWidth(TICK_STROKE_WIDTH);
                innerTickRadius = outerTickRadius - 5;
            }

            canvas.drawLine(outerTickRadius, yCenter,
                    innerTickRadius, yCenter,
                    tickPaint);

            canvas.rotate(tickRot, xCenter, yCenter);



        }
        canvas.restore();

        canvas.save();
        final float secondsRotation = seconds * 6f;
        canvas.rotate(secondsRotation, xCenter, yCenter);
        canvas.drawLine(xCenter, yCenter - radius + 5,
                xCenter, yCenter,
                secondHandPaint);

//        canvas.drawCircle(xCenter, yCenter, 4, secondHandKnobPaint);

        canvas.restore();
    }
}
