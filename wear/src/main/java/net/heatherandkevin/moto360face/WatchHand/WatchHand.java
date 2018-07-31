package net.heatherandkevin.moto360face.WatchHand;

import android.graphics.Canvas;

public abstract class WatchHand {
    protected float xCenter;
    protected float yCenter;

    public WatchHand(float xCenter, float yCenter) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
    }

    public abstract void draw(Canvas canvas, float rotation);
}
