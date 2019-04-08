package com.bignerdranch.criminalintent;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

public class CrimeFragment extends Fragment {

    private final static String ARG_CRIME_ID = "arg_crime_id";

    private Crime mCrime;

    public CrimeFragment() {
        // Required empty public constructor
    }

    // SOS: The easy way to get the id in onCreate is: getActivity().getIntent().get(FOO), but then
    // the fragment must know the id in the intent is called FOO. A better way is to demand others
    // pass an id as part of the creation of the fragment and to stash that id as an argument that
    // the fragment can later retrieve at any point. That is done in the following static method which
    // everyone must call if they want a CrimeFragment. Unfortunately, the constructor can't be made
    // private (AS complains). Finally, a great advantage of args is that they persist (like an intent).
    // The alternative would be to use a constructor that takes the id as arg and saves it in an instance
    // field. And then I'd have to save/restore this field with the onSaveInstanceState mechanism...
    static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        // SOS: in CrimeActivity we do intent.putExtra(FOO, obj), but Bundle doesn't have such a
        // generic method
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
        Button dateButton = view.findViewById(R.id.date_button);
        dateButton.setText(mCrime.getDate().toString());
        dateButton.setEnabled(false);
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
}
