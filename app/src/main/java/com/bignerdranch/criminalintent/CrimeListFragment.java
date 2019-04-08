package com.bignerdranch.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CrimeListFragment extends Fragment {

    private static final int CHANGED_DETAILS_REQUEST_CODE = 24;

    private CrimeAdapter mCrimeAdapter;
    private RecyclerView mCrimeRecyclerView;
    private int mPositionClicked;

    public CrimeListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        populateRecyclerView();

        return view;
    }

    // SOS: Now we don't need the update in onResume so that's gone. The updating of the UI is done
    // in onActivityResult. updateUI was changed to populateRecyclerView.
    private void populateRecyclerView() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        mCrimeAdapter = new CrimeAdapter(crimeLab.getCrimes());
        mCrimeRecyclerView.setAdapter(mCrimeAdapter);
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
            mDateTextView.setText(crime.getDate().toString());
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            // SOS: getLayoutPosition is definitely the position the user saw when he clicked.
            // getAdapterPosition may be different if a change was underway but the new layout has
            // not yet been shown (Android takes at most 16ms to update the layout).
            mPositionClicked = getLayoutPosition();
            Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId());
            startActivityForResult(intent, CHANGED_DETAILS_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHANGED_DETAILS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mCrimeAdapter.notifyItemChanged(mPositionClicked);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeViewHolder> {

        private final List<Crime> mCrimes;

        CrimeAdapter(List<Crime> crimes) {
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
