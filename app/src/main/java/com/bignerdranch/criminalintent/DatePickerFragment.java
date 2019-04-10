package com.bignerdranch.criminalintent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

// SOS: It's possible to show a bare dialog, but it's better to do it this way (inside a DialogFragment)
// cause the bare dialog vanishes when user rotates the screen...
public class DatePickerFragment extends DialogFragment {

    private static final String ARG_DATE = "arg_date";
    static final String EXTRA_DATE = "extra_date";

    private DatePicker mDatePicker;

    // SOS: I add these 3 fields to fix a bug that's also in the book: When the date is set, the time
    // resets to 00:00:00. Ofc I now have to save/restore them as well...
    private int mHour;
    private int mMinute;
    private int mSecond;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mHour", mHour);
        outState.putInt("mMinute", mHour);
        outState.putInt("mSecond", mSecond);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mHour = savedInstanceState.getInt("mHour");
            mMinute = savedInstanceState.getInt("mMinute");
            mSecond = savedInstanceState.getInt("mSecond");
        }
    }

    // SOS: Again, the best way to pass data to fragments (at least if I have to do it before their
    // view is inflated) is via the newInstance/args method.
    static DatePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // SOS: an alternative to inflating would be to just do: View v = new DatePicker(..). In that
        // case I'd also have to set the id programmatically, because: read SOS in date_picker.xml
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(getActivity())
                .inflate(R.layout.date_picker, null);

        initializeDatePicker(dialogView);

        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setView(dialogView)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();

                        Date date = new GregorianCalendar(year, month, day, mHour, mMinute, mSecond).getTime();
                        sendResult(date);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void initializeDatePicker(View dialogView) {
        Bundle args = getArguments();
        if (args != null) {
            Date date = (Date) args.getSerializable(ARG_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            // SOS: these 2 will be needed for fixing the bug mentioned in a SOS above
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
            mSecond = calendar.get(Calendar.SECOND);

            mDatePicker = dialogView.findViewById(R.id.date_picker);
            mDatePicker.init(year, month, day, null);
        }
    }

    private void sendResult(Date date) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        // SOS: Note how we call this directly on the other fragment, whereas before in the parent-
        // child situation, this was called by the ActivityManager! Also note how we get the request-
        // code that we set when we called setTargetFragment in CrimeFragment.
        targetFragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
