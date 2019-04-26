package com.bignerdranch.criminalintent;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CrimeListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "saved_subtitle_visible";

    private CrimeAdapter mCrimeAdapter;
    private RecyclerView mCrimeRecyclerView;

    private boolean mSubtitleVisible;

    // SOS: Fragments must be reusable. Previously, a viewholder responded to a click by starting the
    // CrimePagerActivity. That is NOT good. We should let the hosting activity decide which activity
    // to start or what fragment to insert in what container... The best way to do that is to make
    // the activity implement this interface so that the fragment can notify it.
    interface CallBacks {
        void onCrimeSelected(Crime crime);
    }

    private CallBacks mCallBacks;

    public CrimeListFragment() {
        // Required empty public constructor
    }

    // SOS: The only expectation placed on the hosting activity is that it implements this interface
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallBacks = (CallBacks) context;
    }

    // SOS: the book says setting to null when done is good practice.
    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    // SOS: discovered by mistake: activity/fragment does NOT pause when dialog is shown on-top!?
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                // SOS: on tablet, list is always visible, so show new empty crime immediately
                updateUI();
                mCallBacks.onCrimeSelected(crime);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) return;

        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;

        CrimeLab crimeLab = CrimeLab.get(activity);
        int numCrimes = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, numCrimes, numCrimes);
        if (!mSubtitleVisible) {
            subtitle = null;
        }
        actionBar.setSubtitle(subtitle);
    }

    // SOS: previously we called this only on creation and in onResume. Now the list is visible all
    // the time, so it must be updated whenever a crime changes ('in real-time'). Solution: another
    // interface that will enable CrimeFragment to notify this fragment's activity. The activity will
    // then call this method on the fragment. Success.
    void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mCrimeAdapter);
        } else {
            mCrimeAdapter.setCrimes(crimes);
            mCrimeAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private class CrimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTitleTextView;
        private final TextView mDateTextView;
        private final ImageView mSolvedImageView;

        private Crime mCrime;

        CrimeViewHolder(View view) {
            super(view);

            mTitleTextView = view.findViewById(R.id.crime_title);
            mDateTextView = view.findViewById(R.id.crime_date);
            mSolvedImageView = view.findViewById(R.id.crime_solved);
            itemView.setOnClickListener(this);
        }

        void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(crime.getTitle());
            DateUtils.setDateText(mDateTextView, crime.getDate());
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            mCallBacks.onCrimeSelected(mCrime);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeViewHolder> {

        private List<Crime> mCrimes;

        CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeViewHolder crimeViewHolder, int position) {
            Crime crime = mCrimes.get(position);
            crimeViewHolder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }
}
