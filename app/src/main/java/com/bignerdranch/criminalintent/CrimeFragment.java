package com.bignerdranch.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private final static String ARG_CRIME_ID = "arg_crime_id";
    private final static String DATE_DIALOG_TAG = "DateDialog";
    private final static String TIME_DIALOG_TAG = "TimeDialog";
    private static final int DATE_REQUEST_CODE = 3;
    private static final int TIME_REQUEST_CODE = 4;

    private Crime mCrime;

    private Button mDateButton;
    private Button mTimeButton;

    public CrimeFragment() {
        // Required empty public constructor
    }

    static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            UUID crimeId = (UUID) args.getSerializable(ARG_CRIME_ID);
            mCrime = CrimeLab.get(getContext()).getCrime(crimeId);
        } else {
            Activity activity = getActivity();
            Toast.makeText(activity, "someone forgot to set the arguments for CrimeFragment!",
                    Toast.LENGTH_SHORT).show();
            if (activity != null) {
                activity.finish();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        setupTitleField(view);
        setupDateButton(view);
        setupTimeButton(view);
        setupSolvedCheckbox(view);

        return view;
    }

    private void setupTitleField(View view) {
        EditText titleField = view.findViewById(R.id.crime_title);
        titleField.setText(mCrime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupDateButton(View view) {
        mDateButton = view.findViewById(R.id.date_button);
        setDateButtonText(mCrime.getDate());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    DialogFragment dialogFragment = DatePickerFragment.newInstance(mCrime.getDate());
                    dialogFragment.setTargetFragment(CrimeFragment.this, DATE_REQUEST_CODE);
                    dialogFragment.show(fragmentManager, DATE_DIALOG_TAG);
                }
            }
        });
    }

    private void setupTimeButton(View view) {
        mTimeButton = view.findViewById(R.id.time_button);
        setTimeButtonText(mCrime.getDate());
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    DialogFragment dialogFragment = TimePickerFragment.newInstance(mCrime.getDate());
                    dialogFragment.setTargetFragment(CrimeFragment.this, TIME_REQUEST_CODE);
                    dialogFragment.show(fragmentManager, TIME_DIALOG_TAG);
                }
            }
        });
    }

    private void setDateButtonText(Date date) {
        java.text.DateFormat dateFormat = DateFormat.getMediumDateFormat(getActivity());
        mDateButton.setText(dateFormat.format(date));
    }

    private void setTimeButtonText(Date date) {
        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(getActivity());
        mTimeButton.setText(timeFormat.format(date));
    }

    private void setupSolvedCheckbox(View view) {
        CheckBox solvedCheckbox = view.findViewById(R.id.solved_checkbox);
        solvedCheckbox.setChecked(mCrime.isSolved());
        solvedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == DATE_REQUEST_CODE) {
            Date newDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(newDate);
            setDateButtonText(newDate);
        } else if (requestCode == TIME_REQUEST_CODE) {
            // SOS: newDate will have the right hour/minute and year/month/day=0, so I have to add them.
            Date newDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            addYearMonthDay(mCrime.getDate(), newDate);
            mCrime.setDate(newDate);
            setTimeButtonText(newDate);
        }
    }

    private void addYearMonthDay(Date srcDate, Date destDate) {
        // SOS: ok, these are all deprecated, but fixing them makes the code harder to understand so...
        destDate.setYear(srcDate.getYear());
        destDate.setMonth(srcDate.getMonth());
        destDate.setDate(srcDate.getDate());
    }
}
