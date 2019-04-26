package com.bignerdranch.criminalintent;

import android.text.format.DateFormat;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

class DateUtils {

    static void setDateText(TextView textView, Date date) {
        String dateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMddEEEHHmm");
        String dateString = DateFormat.format(dateFormat, date).toString();
        textView.setText(dateString);
    }
}
