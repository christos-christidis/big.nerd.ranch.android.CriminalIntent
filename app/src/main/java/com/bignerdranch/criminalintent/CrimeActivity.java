package com.bignerdranch.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeActivity extends SingleFragmentActivity {

    @Override
    Fragment createFragment() {
        return new CrimeFragment();
    }
}
