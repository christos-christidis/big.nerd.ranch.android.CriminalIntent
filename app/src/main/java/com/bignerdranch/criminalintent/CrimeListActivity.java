package com.bignerdranch.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    Fragment createFragment() {
        return new CrimeListFragment();
    }
}
