package net.heatherandkevin.moto360face.SubDial;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import net.heatherandkevin.moto360face.TickMark.TickMark;

public abstract class SubDial {
    protected float xCenter;
    protected float yCenter;
    protected float radius;

    protected Paint fill;
    protected Paint outline;

    private static final int FILL_COLOR = Color.BLACK;
    private static final int FILL_ALPHA = 125;
    private static final int GRADIENT_START_COLOR = Color.BLACK;
    private static final int GRADIENT_END_COLOR = Color.WHITE;

    private static final float STROKE_WIDTH = 2f;


    public SubDial(float xCenter, float yCenter, float radius) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;

        initColors();
    }

    protected void updateXY(float xCenter, float yCenter) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;

        initColors();
    }

    public float getxCenter() {
        return xCenter;
    }

    public float getyCenter() {
        return yCenter;
    }

    protected void initColors() {
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

    public abstract void draw(Canvas canvas);
}
