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

public class DatePickerFragment extends DialogFragment {

    private static final String ARG_DATE = "arg_date";
    static final String EXTRA_DATE = "extra_date";

    private DatePicker mDatePicker;

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

                        Date date = new GregorianCalendar(year, month, day).getTime();
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
        targetFragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
