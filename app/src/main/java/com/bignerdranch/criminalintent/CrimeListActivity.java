package com.bignerdranch.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.CallBacks, CrimeFragment.Callbacks {

    // SOS: we override the default layout and use a ref which will point to 2 different layouts, a
    // single-fragment one for the phone and a two-pane one for the tablet.
    @Override
    protected int getLayoutResId() {
        return R.layout.app_layout;
    }

    // SOS: In both cases, there's a frame-layout w id=fragment_container on which this activity will
    // always add a CrimeListFragment. This happens in SingleFragmentActivity's onCreate.
    @Override
    Fragment createFragment() {
        return new CrimeListFragment();
    }

    // SOS: called from the list whenever user clicks on an existing crime or the 'Add crime' menu
    // option. In both cases, we want to show a CrimeFragment (either in a new activity for the phone,
    // or in the right pane for the tablet)
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

    // SOS: called from CrimeFragment when data changes inside it. On the tablet, CrimeFragment is
    // hosted by this activity, which will receive the call and call updateUI on the list. On the
    // phone, the host will be CrimePagerActivity which will receive the call but do nothing.
    @Override
    public void onCrimeUpdated() {
        CrimeListFragment listFragment = (CrimeListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.updateUI();
        }
    }
}
