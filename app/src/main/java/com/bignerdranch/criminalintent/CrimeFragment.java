package com.bignerdranch.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
    private static final int DATE_REQUEST_CODE = 3;
    private static final int CONTACT_REQUEST_CODE = 4;

    private Crime mCrime;

    private Button mDateButton;
    private Button mChooseSuspectButton;

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
        setupSolvedCheckbox(view);
        setUpSendReportButton(view);
        setUpChooseSuspectButton(view);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
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
        mDateButton.setText(mCrime.getDate().toString());
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

    private void setUpSendReportButton(View view) {
        Button sendReportButton = view.findViewById(R.id.send_crime_report);
        sendReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                intent = Intent.createChooser(intent, getString(R.string.send_report_via));
                startActivity(intent);
            }
        });
    }

    private void setUpChooseSuspectButton(View view) {
        mChooseSuspectButton = view.findViewById(R.id.choose_suspect);
        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        if (!existsContactsApp(intent)) {
            mChooseSuspectButton.setEnabled(false);
            return;
        }

        mChooseSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(intent, CONTACT_REQUEST_CODE);
            }
        });

        if (mCrime.getSuspect() != null) {
            mChooseSuspectButton.setText(mCrime.getSuspect());
        }
    }

    // SOS: if there's no activity to handle the intent, Android throws an exception. This check makes
    // sure there's an app. The other way to avoid an exception is to use Intent.createChooser
    private boolean existsContactsApp(Intent intent) {
        Activity activity = getActivity();
        if (activity == null) {
            return false;
        }

        PackageManager packageManager = getActivity().getPackageManager();
        return packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == DATE_REQUEST_CODE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(mCrime.getDate().toString());
        } else if (requestCode == CONTACT_REQUEST_CODE && data != null) {
            // SOS: I only need access to a single contact. Contacts app grants me a one-time
            // permission to read this URI, which is why don't have to explicitly ask for permission.
            // Specifically, it adds Intent.FLAG_GRANT_READ_URI_PERMISSION to the intent it returns.
            Uri contactUri = data.getData();
            if (contactUri == null) {
                return;
            }

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            // SOS: contactUri refers to a single contact so cursor will return a single row with 1 field
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

            try (Cursor cursor = activity.getContentResolver().query(contactUri, queryFields,
                    null, null, null)) {
                if (cursor == null || cursor.getCount() == 0) {
                    return;
                }
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                mCrime.setSuspect(suspect);
                mChooseSuspectButton.setText(suspect);
            }
        }
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspectString;
        if (mCrime.getSuspect() == null) {
            suspectString = getString(R.string.crime_report_no_suspect);
        } else {
            suspectString = getString(R.string.crime_report_suspect, mCrime.getSuspect());
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspectString);

        return report;
    }
}
