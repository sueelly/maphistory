package com.example.maphistory;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.maphistory.database.DBManager;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class Fragment3 extends Fragment {


    ViewGroup rootView;
    MaterialCalendarView materialCalendarView;
    ArrayList<CalendarDay> dates = new ArrayList<>();
    ArrayList<Note> items = new ArrayList<Note>();
    DBManager dbHelper;
    Context context;
    Fragment1 fragmentNew;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_3, container, false);

        dbHelper = new DBManager(context, 1);
        items = dbHelper.loadNoteList();

        materialCalendarView = rootView.findViewById(R.id.calendarView);
        materialCalendarView.setSelectedDate(CalendarDay.today());

        List<CalendarDay> calendarDays = getDates();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new EventDecorator(Color.RED, calendarDays, getActivity()),
                new TodayDecorator()
        );

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                boolean check = false;
                check = calendarDays.contains(date);
                if(check){

                    String mm = setMonthDay(( date.getMonth() +1 )) + "";
                    String yyyy = date.getYear() + "";
                    String dd = setMonthDay(date.getDay()) + "";
                    String yyyyMMdd = yyyy +mm + dd ;

                    int ind = dates.indexOf(date);

                    fragmentNew = new Fragment1();
                    fragmentNew.setItem(items.get(ind));

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragmentNew).commit();

                }
                else {
                    Intent intent = new Intent(getContext(), NewAndListActivity.class);

                    Fragment1 fragment1 = new Fragment1();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment1).commit();

                }
            }
        });

        return rootView;

    }

    private String setMonthDay(int num) {
        if(num <10)
            return "0" + num;
        else
            return Integer.toString(num);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (context != null) {
            context = null;
        }
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

            calendar.add(Calendar.MONTH, -3); //3달전부터

//            for (int i = 0; i < 30; i++) {
//                CalendarDay day = CalendarDay.from(calendar);
//
//
//                dates.add(day);
//
//
//                calendar.add(Calendar.DATE, 5); //5일 간격으로 CalendarDay 타입 추가
//
//            }

            for(Note i: items) {

                Date datee = null;

                try{
                    SimpleDateFormat simple_format = new SimpleDateFormat("yyyyMMdd");
                    String timeString = i.createDateStr;
                    datee = simple_format.parse(timeString);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                CalendarDay day = CalendarDay.from(datee);
                dates.add(day);


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