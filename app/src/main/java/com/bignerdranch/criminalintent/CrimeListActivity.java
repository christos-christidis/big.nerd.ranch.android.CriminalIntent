package com.bignerdranch.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.CallBacks, CrimeFragment.Callbacks {

    @Override
    protected int getLayoutResId() {
        return R.layout.app_layout;
    }

    @Override
    Fragment createFragment() {
        return new CrimeListFragment();
    }

    // SOS: remember, the "main" fragment's already been added in SingleFragmentActivity's onCreate
    @Override
    public void onCrimeSelected(Crime crime) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
            startActivity(intent);
        } else {
            Fragment detailFragment = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, detailFragment)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated() {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.updateUI();
        }
    }
}
