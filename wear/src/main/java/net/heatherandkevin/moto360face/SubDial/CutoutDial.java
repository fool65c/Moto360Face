package net.heatherandkevin.moto360face.SubDial;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;

public class CutoutDial extends SubDial {
    private Path facePath;
    private float diameter;

    public CutoutDial(float xOffsetCenter, float yOffsetCenter, float radiusOffset,
                      float desiredRadius, float buffer, CutoutSide faceSide) {
        super(0,0, desiredRadius);

        float cutoutLeft = xOffsetCenter - radiusOffset - buffer;
        float cutoutTop = yOffsetCenter - radiusOffset - buffer;
        float cutoutRight = xOffsetCenter + radiusOffset + buffer;
        float cutoutBottom = yOffsetCenter + radiusOffset + buffer;

        RectF cutoutRect = new RectF(cutoutLeft, cutoutTop, cutoutRight, cutoutBottom);
        float adjacent = radiusOffset + buffer;
        float opposite = radiusOffset + buffer - radius;
        float cutOutStartAngle = (float) Math.toDegrees(Math.atan((double) opposite / adjacent));

        float pullDown = (radiusOffset + buffer) + (radiusOffset + buffer) * (float) Math.cos((double) cutOutStartAngle);

        float left;
        float top;
        float bottom;
        float right;
        RectF faceRect;
        facePath = new Path();
        if (faceSide == CutoutSide.RIGHT) {
            left = cutoutLeft - radius + pullDown + 3;
            top = cutoutTop - radius + pullDown + 3;
            bottom = top + 2 * radius;
            right = left +  2 * radius;
            faceRect = new RectF(left, top, right, bottom);
            facePath.arcTo(cutoutRect, 270 - cutOutStartAngle, 2 * cutOutStartAngle - 90, false);
            facePath.arcTo(faceRect, 90, 270, false);
        } else {
            right = cutoutRight + radius - pullDown - 3;
            top = cutoutTop - radius + pullDown + 3;
            bottom = top + 2 * radius;
            left = right -  2 * radius;
            faceRect = new RectF(left, top, right, bottom);
            facePath.arcTo(cutoutRect, 0 - cutOutStartAngle, 2 * cutOutStartAngle - 90, false);
            facePath.arcTo(faceRect, 180, 270, false);
        }
        facePath.close();

        diameter = faceRect.width();

        updateXY(faceRect.centerX(), faceRect.centerY());
    }

    public Path getFacePath() {
        return facePath;
    }

    public float getDiameter() {
        return diameter;
    }

    public void draw(Canvas canvas) {
        canvas.drawPath(facePath, outline);
        canvas.drawPath(facePath, fill);
    }
}
