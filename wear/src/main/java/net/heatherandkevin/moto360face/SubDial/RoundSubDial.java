package net.heatherandkevin.moto360face.SubDial;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

public class RoundSubDial extends SubDial {

    public RoundSubDial(float xCenter, float yCenter, float radius) {
        super(xCenter, yCenter, radius);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle(xCenter, yCenter,
                radius,
                outline);

        canvas.drawCircle(xCenter, yCenter,
                radius,
                fill);
    }
}
