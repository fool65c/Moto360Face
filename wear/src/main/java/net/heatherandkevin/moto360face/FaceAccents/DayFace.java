package net.heatherandkevin.moto360face.FaceAccents;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.SparseArray;

import net.heatherandkevin.moto360face.SubDial.CutoutDial;
import net.heatherandkevin.moto360face.SubDial.CutoutSide;
import net.heatherandkevin.moto360face.SubDial.SubDial;
import net.heatherandkevin.moto360face.TickMark.BasicTickMarks;
import net.heatherandkevin.moto360face.WatchHand.BasicHand;
import net.heatherandkevin.moto360face.WatchHand.WatchHand;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DayFace {
    private CutoutDial subDial;
    private WatchHand dayHand;
    private Paint dayNamePaint;

    private Path textPath;
    private int dayOfWeek;

    private List<String> dayNames;
    private HashMap<String, Float> dayNameHOffset;
    private HashMap<Integer, Float> dayNameCenter;

    private static final float DAY_NAME_TEXT_SIZE = 13.5f;

    private static final int DAY_PAINT_COLOR = Color.WHITE;

    public DayFace (float xOffsetCenter, float yOffsetCenter, float radiusOffset,
                       float desiredRadius, float buffer) {

        initPaint();
        subDial = new CutoutDial(xOffsetCenter, yOffsetCenter, radiusOffset,
                desiredRadius, buffer, CutoutSide.RIGHT);

        calculateDayLength(desiredRadius);
        textPath = subDial.getFacePath();

        dayHand = new BasicHand(subDial.getxCenter(), subDial.getyCenter(),
                desiredRadius - DAY_NAME_TEXT_SIZE);
    }

    private void initPaint() {
        dayNamePaint = new Paint();
        dayNamePaint.setColor(DAY_PAINT_COLOR);
        dayNamePaint.setTextSize(DAY_NAME_TEXT_SIZE);
        dayNamePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        dayNamePaint.setStrokeWidth(0.75f);
        dayNamePaint.setAntiAlias(true);
    }

    private void calculateDayLength(float radius) {
        dayNames = Arrays.asList("SUN", "MON", "TUE", "WED", "THR", "FRI", "SAT");
        HashMap<String, Float> dayNameLength = new HashMap<>();

        PathMeasure pathMeasure = new PathMeasure(subDial.getFacePath(), true);
        float totalSpaceAvailable = pathMeasure.getLength() - 65f;

        float totalSpaceTaken = 0;
        float length;
        for (String day : dayNames) {
            length = dayNamePaint.measureText(day);
            totalSpaceTaken += length;
            dayNameLength.put(day, length);
        }


        //Calculate the start position of each day name
        float dayBuffer = (totalSpaceAvailable - totalSpaceTaken ) / 14f;
        dayNameHOffset = new HashMap<>();
        dayNameCenter = new HashMap<>();

        float offset;
        float lastEnd = 65;
        for (String day : dayNames) {
            offset = lastEnd;
            dayNameHOffset.put(day, offset);
            lastEnd += dayNameLength.get(day) + 2 * dayBuffer;
        }

        //calculate the angle to the center of each day name
        float dayAngle = 17.5f;
        float startingDistance = 65;
        float distance;
        float radiusInput = 2f * (float) Math.pow(subDial.getDiameter() / 2f, 2);
        for (String day : dayNames) {
            if (day.equals("SUN")) {
                dayNameCenter.put(dayNames.indexOf(day) + 1, dayAngle);
                startingDistance = dayNameHOffset.get(day) + dayNameLength.get(day) / 2f;
            } else {
                distance = dayNameHOffset.get(day) + dayNameLength.get(day) / 2f;
                dayAngle += Math.toDegrees(Math.acos((radiusInput - Math.pow(distance - startingDistance, 2)) / radiusInput));
                dayNameCenter.put(dayNames.indexOf(day) + 1, dayAngle);
                startingDistance = distance;
            }
        }
    }

    public void setCalendar(Calendar calendar) {
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void draw (Canvas canvas) {
        subDial.draw(canvas);

        for (String day : dayNames) {
            canvas.drawTextOnPath(day, textPath,
                    dayNameHOffset.get(day),
                    DAY_NAME_TEXT_SIZE,
                    dayNamePaint);
        }

        dayHand.draw(canvas, dayNameCenter.get(dayOfWeek));
    }
}
