package com.example.maphistory;


import static com.example.maphistory.Fragment1.date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.maphistory.database.DBManager;

import java.util.Calendar;

public class SelectDateFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public static String DATE="";
    public static Calendar CALENDER;
    DBManager dbHelper;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        dbHelper = new DBManager(getActivity(), 1);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        CALENDER = c;

        return new DatePickerDialog(getActivity(),this,year,month,day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

        processDatePickerResult(year,month,day);
        date.setText(DATE);
    }

    public void processDatePickerResult(int year, int month, int day){
        String month_string = setMonthDay(month+1);
        String day_string = setMonthDay(day);
        String year_string = Integer.toString(year);
        String dateMessage = (year_string + "" + month_string +"" + day_string);

        Toast.makeText(getContext(), "Date: " + dateMessage, Toast.LENGTH_SHORT).show();
        DATE = dateMessage;

    }

    private String setMonthDay(int num) {
        if(num <10)
            return "0" + num;
        else
            return Integer.toString(num);
    }

}