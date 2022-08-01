package com.example.maphistory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class HistoryCalendarActivity extends AppCompatActivity {
    MaterialCalendarView materialCalendarView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.history_calendar);

        materialCalendarView = findViewById(R.id.calendarView);
        materialCalendarView.setSelectedDate(CalendarDay.today());

        List<CalendarDay> calendarDays = getDates();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new EventDecorator(Color.RED, calendarDays, HistoryCalendarActivity.this),
                new TodayDecorator()
        );

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                boolean check = false;
                check = calendarDays.contains(date);
                if(check){
                    Intent intent = new Intent(HistoryCalendarActivity.this, MainActivity.class); //나중에 해당 일기페이지로 이동하도록 설정
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), NewHistoryActivity.class); //나중에 해당 일기페이지로 이동하도록 설정
                    startActivity(intent);
                }
            }
        });
    }

    List<CalendarDay> getDates(){
        ApiSimulator apiSimulator = new ApiSimulator();
        return apiSimulator.doInBackground();
    }
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        @Override
        protected ArrayList<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            //이번에는 직접 설정한 데이트값인데, 나중에 db랑 연동해서 값 넣으면 될듯
            calendar.add(Calendar.MONTH, -2); //2달전부터
            ArrayList<CalendarDay> dates = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
                calendar.add(Calendar.DATE, 5); //5일 간격으로 CalendarDay 타입 추가

            }

            return dates;
        }


    }
}
class SaturdayDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();

    public SaturdayDecorator() {}

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SATURDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.BLUE));
    }
}

class SundayDecorator implements DayViewDecorator {

    private final Calendar calendar = Calendar.getInstance();

    public SundayDecorator() {}

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        day.copyTo(calendar);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.RED));
    }
}

class EventDecorator implements DayViewDecorator {
    private final Drawable drawable;
    private final int color;
    private final HashSet<CalendarDay> dates;


    @SuppressLint("UseCompatLoadingForDrawables")
    public EventDecorator(int color, Collection<CalendarDay> dates, Activity context) {

        drawable = context.getResources().getDrawable(R.drawable.calendar_background);

        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
        view.addSpan(new DotSpan(5, color));

    }
}

class TodayDecorator implements DayViewDecorator {

    private CalendarDay date;

    public TodayDecorator() {
        date = CalendarDay.today();
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.addSpan(new RelativeSizeSpan(1.4f));
        view.addSpan(new ForegroundColorSpan(Color.GREEN));
    }

    public void setDate(Date date) {
        this.date = CalendarDay.from(date);
    }
}

