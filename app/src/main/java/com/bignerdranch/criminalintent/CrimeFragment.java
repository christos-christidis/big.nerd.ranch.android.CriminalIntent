package com.bignerdranch.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private final static String ARG_CRIME_ID = "arg_crime_id";

    private final static String DATE_DIALOG_TAG = "DateDialog";
    private final static String PHOTO_DIALOG_TAG = "PhotoDialog";

    private static final int DATE_REQUEST_CODE = 3;
    private static final int CONTACT_REQUEST_CODE = 4;
    private static final int PHOTO_REQUEST_CODE = 5;

    private Crime mCrime;

    private Button mDateButton;
    private Button mChooseSuspectButton;
    private ImageView mPhotoView;
    private File mPhotoFile;

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
            CrimeLab crimeLab = CrimeLab.get(getContext());
            mCrime = crimeLab.getCrime(crimeId);
            mPhotoFile = crimeLab.getPhotoFile(mCrime, getActivity());
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

        setUpTitleField(view);
        setUpDateButton(view);
        setUpSolvedCheckbox(view);
        setUpSendReportButton(view);
        setUpChooseSuspectButton(view);
        setUpPhotoView(view);
        setUpCameraButton(view);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    private void setUpTitleField(View view) {
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

    private void setUpDateButton(View view) {
        mDateButton = view.findViewById(R.id.date_button);
        mDateButton.setText(mCrime.getDate().toString());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = DatePickerFragment.newInstance(mCrime.getDate());
                dialogFragment.setTargetFragment(CrimeFragment.this, DATE_REQUEST_CODE);
                showDialogFragment(dialogFragment, DATE_DIALOG_TAG);
            }
        });
    }

    private void setUpSolvedCheckbox(View view) {
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

    private void setUpPhotoView(View view) {
        mPhotoView = view.findViewById(R.id.crime_photo);
        updatePhotoView();
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileExists(mPhotoFile)) {
                    DialogFragment dialogFragment = BiggerImageFragment.newInstance(mPhotoFile);
                    showDialogFragment(dialogFragment, PHOTO_DIALOG_TAG);
                } else {
                    Toast.makeText(getActivity(), "No image to display!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDialogFragment(DialogFragment dialogFragment, String fragmentTag) {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            dialogFragment.show(fragmentManager, fragmentTag);
        }
    }

    private void setUpCameraButton(View view) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        ImageButton cameraButton = view.findViewById(R.id.camera_button);
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        final PackageManager packageManager = activity.getPackageManager();

        boolean canTakePhoto = mPhotoFile != null &&
                intent.resolveActivity(packageManager) != null;

        cameraButton.setEnabled(canTakePhoto);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(activity,
                        "com.bignerdranch.criminalintent.fileprovider", mPhotoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = packageManager.queryIntentActivities(
                        intent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo info : cameraActivities) {
                    activity.grantUriPermission(info.activityInfo.packageName, uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                startActivityForResult(intent, PHOTO_REQUEST_CODE);
            }
        });
    }

    private boolean existsContactsApp(Intent intent) {
        if (getActivity() == null) return false;
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

            if (contactUri == null || getActivity() == null) return;

            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};

            try (Cursor cursor = getActivity().getContentResolver().query(contactUri, queryFields,
                    null, null, null)) {
                if (hasNoData(cursor)) {
                    return;
                }
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                mCrime.setSuspect(suspect);
                mChooseSuspectButton.setText(suspect);
            }
        } else if (requestCode == PHOTO_REQUEST_CODE) {
            if (getActivity() == null) return;
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.criminalintent.fileprovider", mPhotoFile);

            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            updatePhotoView();
        }
    }

    private boolean hasNoData(Cursor cursor) {
        return cursor == null || cursor.getCount() == 0;
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

    private void updatePhotoView() {
        if (fileExists(mPhotoFile)) {
            if (getActivity() == null) return;
            Bitmap bitmap = PictureUtils.getConservativeEstimateBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        } else {
            mPhotoView.setImageDrawable(null);
        }
    }

    private boolean fileExists(File file) {
        return file != null && file.exists();
    }
}
