package com.changui0.will_it_rain;

import android.content.Context;

public class MyBackgroundThread extends Thread {
    private Context context;
    private long now;
    public String resultStr = "result";

    public void setContext(Context context) {
        this.context = context;
    }

    public void setNow(long now) {
        this.now = now;
    }

    @Override
    public void run() {
        Weather wth = new Weather();
        int pop = -1;
        if (MyGps.isXyValid()) {
            pop = wth.isGoingToRain(24, MyGps.x, MyGps.y, now);
            if (!Weather.isPopValid(pop)) {
                pop = wth.isGoingToRain(24, MyGps.x, MyGps.y, now);
            }

        } else pop = Weather.ERROR_CODE.INVALID_XY.getVal();

        resultStr = wth.makeNotificatoinText(24, pop);
    }
}
