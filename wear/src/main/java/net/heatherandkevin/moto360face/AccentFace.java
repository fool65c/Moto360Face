package net.heatherandkevin.moto360face;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.SweepGradient;

class AccentFace {
    protected Paint fill;
    protected Paint outline;

    protected float xCenter;
    protected float yCenter;
    protected float radius;
    private Path facePath;

    private static final int FILL_COLOR = Color.BLACK;
    private static final int FILL_ALPHA = 125;
    private static final int GRADIENT_START_COLOR = Color.BLACK;
    private static final int GRADIENT_END_COLOR = Color.WHITE;

    private static final float STROKE_WIDTH = 2f;
    private static final float FACE_BUFFER = 10f;


    public AccentFace(float xCenter, float yCenter, float radius,
                      float xCutOut, float yCutOut, float rCutOut) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;

        float magicStart;
        float magicSweep;
        float magicFaceStart;
        float magicFaceSweep;
        if (xCenter > xCutOut) {
            magicStart = 270;
            magicSweep = 56.5f;
            magicFaceStart = 87.5f;
            magicFaceSweep = -271.5f;
        } else {
            magicStart = 260 + 56.5f;
            magicSweep = -56.5f;
            magicFaceStart = 180-87.5f;
            magicFaceSweep = 271.5f;
        }

        float adjacent = rCutOut + FACE_BUFFER;
        float opposite = xCenter - radius - xCutOut;
        float angle = (float) Math.toDegrees(Math.atan((double) opposite / adjacent)) + magicStart;

        facePath = new Path();
        facePath.arcTo(xCutOut- rCutOut- FACE_BUFFER, yCutOut- rCutOut - FACE_BUFFER,
                xCutOut + rCutOut + FACE_BUFFER, yCutOut+ rCutOut + FACE_BUFFER,
                angle,magicSweep, false);
        facePath.arcTo(xCenter - radius, yCenter - radius,
                xCenter + radius, yCenter + radius,
                magicFaceStart,magicFaceSweep, false);
        facePath.close();

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

    }

    public void draw (Canvas canvas) {
        canvas.drawPath(facePath, outline);
        canvas.drawPath(facePath, fill);
    }
}
