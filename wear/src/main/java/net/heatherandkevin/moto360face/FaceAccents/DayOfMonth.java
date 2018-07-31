package net.heatherandkevin.moto360face.FaceAccents;

import android.graphics.Canvas;

import net.heatherandkevin.moto360face.SubDial.CutoutDial;
import net.heatherandkevin.moto360face.SubDial.CutoutSide;
import net.heatherandkevin.moto360face.SubDial.SubDial;
import net.heatherandkevin.moto360face.TickMark.BasicTickMarks;
import net.heatherandkevin.moto360face.WatchHand.BasicHand;
import net.heatherandkevin.moto360face.WatchHand.WatchHand;

import java.util.Calendar;

public class DayOfMonth {
    private SubDial subDial;
    private BasicTickMarks tickMarks;
    private WatchHand dayHand;
    private float daysInMonth;
    private float currentDay;
    private float handRotation;


    public DayOfMonth (float xOffsetCenter, float yOffsetCenter, float radiusOffset,
                       float desiredRadius, float buffer) {
        subDial = new CutoutDial(xOffsetCenter, yOffsetCenter, radiusOffset,
                desiredRadius, buffer, CutoutSide.LEFT);

        tickMarks = new BasicTickMarks(subDial.getxCenter(), subDial.getyCenter(),
                subDial.getyCenter() + desiredRadius - 3, 5,
                daysInMonth, 95, 355);

        dayHand = new BasicHand(subDial.getxCenter(), subDial.getyCenter(), desiredRadius);
    }

    public void setCalendar(Calendar calendar) {
        if (currentDay != calendar.get(Calendar.DAY_OF_MONTH)) {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            tickMarks.setCount(daysInMonth);
            handRotation = tickMarks.getTickAngle() * currentDay + 95f;
        }
    }

    public void draw(Canvas canvas) {
        subDial.draw(canvas);
        tickMarks.draw(canvas);
        dayHand.draw(canvas, handRotation);
    }
}
