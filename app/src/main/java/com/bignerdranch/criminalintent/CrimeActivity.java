package com.bignerdranch.criminalintent;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CrimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime);

        FragmentManager fragmentManager = getSupportFragmentManager();

        // SOS: fragmentManager keeps a list of fragments identified by the ID of their container(!),
        // plus a backstack of transactions. If the activity is recreated due to config change, he
        // automatically re-inserts all previously inserted fragments to the correct places. OTOH,
        // if the activity is brand-new, obviously I won't find the fragment in fragment_container.
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = new CrimeFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
