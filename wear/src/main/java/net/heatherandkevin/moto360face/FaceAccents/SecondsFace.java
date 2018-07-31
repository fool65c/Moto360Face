package net.heatherandkevin.moto360face.FaceAccents;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import net.heatherandkevin.moto360face.SubDial.RoundSubDial;
import net.heatherandkevin.moto360face.SubDial.SubDial;
import net.heatherandkevin.moto360face.TickMark.BasicTickMarks;
import net.heatherandkevin.moto360face.WatchHand.BasicHand;
import net.heatherandkevin.moto360face.WatchHand.WatchHand;

public class SecondsFace {
    private BasicTickMarks tickMarks;
    private SubDial subDial;
    private WatchHand hand;

    public SecondsFace(float xCenter, float yCenter, float radius) {
        subDial = new RoundSubDial(xCenter, yCenter, radius);

        tickMarks = new BasicTickMarks(xCenter, yCenter,
                yCenter + radius - 3, 5,
                60, 0, 360);
        hand = new BasicHand(xCenter, yCenter, radius);
    }

    public void draw (Canvas canvas, float seconds) {
        // draw background
        subDial.draw(canvas);

        // tick marks
        tickMarks.draw(canvas);

        //draw hand
        hand.draw(canvas, seconds * 6f);
    }
}
