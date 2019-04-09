package com.bignerdranch.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private static final String EXTRA_CRIME_ID = "com.bignerdranch.crime_id";

    private List<Crime> mCrimes;

    private ViewPager mPager;
    private Button mGoToFirstButton;
    private Button mGoToLastButton;

    static Intent newIntent(Context context, UUID crimeId) {
        Intent intent = new Intent(context, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mCrimes = CrimeLab.get(this).getCrimes();

        setUpPager();
        setUpButtons();
    }

    private void setUpPager() {
        mPager = findViewById(R.id.crime_view_pager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPager.setAdapter(new CrimePagerAdapter(fragmentManager));

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        for (int i = 0; i < mCrimes.size(); i++) {
            Crime crime = mCrimes.get(i);
            if (crime.getId().equals(crimeId)) {
                mPager.setCurrentItem(i);
                break;
            }
        }

        mPager.setOnPageChangeListener(this);
    }

    private void setUpButtons() {
        mGoToFirstButton = findViewById(R.id.go_to_first);
        mGoToLastButton = findViewById(R.id.go_to_last);
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.go_to_first:
                mPager.setCurrentItem(0);
                break;
            case R.id.go_to_last:
                mPager.setCurrentItem(mCrimes.size() - 1);
                break;
        }
    }

    // SOS: OnPageChangeListener interface methods
    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        mGoToFirstButton.setEnabled(true);
        mGoToLastButton.setEnabled(true);

        if (i == 0) {
            mGoToFirstButton.setEnabled(false);
        }
        if (i == mCrimes.size() - 1) {
            mGoToLastButton.setEnabled(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    private class CrimePagerAdapter extends FragmentStatePagerAdapter {
        CrimePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Crime crime = mCrimes.get(position);
            return CrimeFragment.newInstance(crime.getId());
        }

        @Override
        public int getCount() {
            return mCrimes.size();
        }
    }
}
