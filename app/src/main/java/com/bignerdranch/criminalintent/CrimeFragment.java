package com.bignerdranch.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
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

import static android.provider.ContactsContract.*;

public class CrimeFragment extends Fragment {

    private final static String PERMISSION_STRING = Manifest.permission.READ_CONTACTS;
    private final static int PERMISSION_REQUEST_CODE = 369;

    private final static String ARG_CRIME_ID = "arg_crime_id";
    private final static String DATE_DIALOG_TAG = "DateDialog";
    private static final int DATE_REQUEST_CODE = 3;
    private static final int CONTACT_REQUEST_CODE = 4;

    private Crime mCrime;

    private Button mDateButton;
    private Button mChooseSuspectButton;
    private Button mDialButton;

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
        setUpDialButton(view);
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
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }

                Intent intent = ShareCompat.IntentBuilder.from(activity)
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(R.string.send_report_via)
                        .createChooserIntent();

                startActivity(intent);
            }
        });
    }

    private void setUpChooseSuspectButton(View view) {
        mChooseSuspectButton = view.findViewById(R.id.choose_suspect);
        final Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);

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

    private void setUpDialButton(View view) {
        mDialButton = view.findViewById(R.id.dial_suspect);

        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCrime.getPhoneNumber() != null) {
                    Uri phoneUri = Uri.parse("tel:" + mCrime.getPhoneNumber());
                    Intent intent = new Intent(Intent.ACTION_DIAL, phoneUri);
                    startActivity(intent);
                }
            }
        });

        if (mCrime.getPhoneNumber() != null) {
            mDialButton.setText(mCrime.getPhoneNumber());
        }
    }

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
            Uri contactUri = data.getData();
            if (contactUri == null) {
                return;
            }

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            // SOS: I simplify things by getting all columns (ie no projection)
            ContentResolver contentResolver = activity.getContentResolver();
            try (Cursor cursor = contentResolver.query(contactUri, null,
                    null, null, null)) {

                if (hasNoData(cursor)) {
                    return;
                }
                cursor.moveToFirst();
                String suspect = cursor.getString(cursor.getColumnIndex(
                        Contacts.DISPLAY_NAME));
                mCrime.setSuspect(suspect);
                mChooseSuspectButton.setText(suspect);

                // SOS: make sure to read note on method below
                if (!havePermission(activity)) {
                    return;
                }

                String hasPhoneNumber = getString(cursor, Contacts.HAS_PHONE_NUMBER);
                String lookupKey = getString(cursor, Contacts.LOOKUP_KEY);

                if (hasPhoneNumber.equals("1")) {
                    String phoneNumber = getPhoneNumber(contentResolver, lookupKey);
                    mCrime.setPhoneNumber(phoneNumber);
                    mDialButton.setText(phoneNumber);
                }
            }
        }
    }

    // SOS: Do NOT use ActivityCompat.requestPermissions when requesting permissions from a fragment,
    // otherwise onRequestPermissionsResult will be called on the activity, not the fragment! Just
    // use plain fragment method requestPermissions.
    private boolean havePermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, PERMISSION_STRING) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERMISSION_STRING}, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private String getPhoneNumber(ContentResolver contentResolver, String lookupKey) {
        try (Cursor phoneCursor = contentResolver.query(
                CommonDataKinds.Phone.CONTENT_URI, null,
                CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                new String[]{lookupKey},
                null)) {

            if (hasNoData(phoneCursor)) {
                return null;
            }
            phoneCursor.moveToFirst();
            return getString(phoneCursor, CommonDataKinds.Phone.NUMBER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Sorry, I'll need permission if you want to dial someone", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Okay, choose suspect again to get number", Toast.LENGTH_SHORT).show();
                mChooseSuspectButton.setText(R.string.choose_suspect);
            }
        }
    }

    // Little helper methods
    private boolean hasNoData(Cursor cursor) {
        return cursor == null || cursor.getCount() == 0;
    }

    private String getString(Cursor cursor, String column) {
        return cursor.getString(cursor.getColumnIndex(column));
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
