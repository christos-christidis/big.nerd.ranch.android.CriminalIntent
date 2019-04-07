package com.bignerdranch.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: supposedly this class is a Singleton because it centralizes control of this list. But this
// particular singleton simply returns the list, that then ANYONE can modify. I'm leaving it as is for
// now until I'm sure it's not needed in later chapters for some reason.
class CrimeLab {

    private static CrimeLab sCrimeLab;

    private final List<Crime> mCrimes;

    private CrimeLab(Context context) {
        mCrimes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 == 0);
            mCrimes.add(crime);
        }
    }

    static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    List<Crime> getCrimes() {
        return mCrimes;
    }

    Crime getCrime(UUID id) {
        for (Crime crime : mCrimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }
        return null;
    }
}
