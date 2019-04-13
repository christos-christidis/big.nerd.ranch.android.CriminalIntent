package com.bignerdranch.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.criminalintent.database.CrimeDbHelper;
import com.bignerdranch.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: supposedly this class is a Singleton because it centralizes control of this list. But this
// particular singleton simply returns the list, that then ANYONE can modify. I'm leaving it as is for
// now until I'm sure it's not needed in later chapters for some reason.
class CrimeLab {

    private static CrimeLab sCrimeLab;

    private final SQLiteDatabase mDb;

    private CrimeLab(Context context) {
        Context applicationContext = context.getApplicationContext();
        mDb = new CrimeDbHelper(applicationContext).getWritableDatabase();
    }

    static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    void addCrime(Crime crime) {
        ContentValues values = getContentValues(crime);
        mDb.insert(CrimeTable.NAME, null, values);
    }

    void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDb.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDb.query(CrimeTable.NAME, null,   // null = select all columns
                whereClause, whereArgs,
                null, null, null);

        return new CrimeCursorWrapper(cursor);
    }

    List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        try (CrimeCursorWrapper cursor = queryCrimes(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.retrieveCrime());
                cursor.moveToNext();
            }
        }

        return crimes;
    }

    Crime getCrime(UUID id) {
        try (CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[]{id.toString()})) {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.retrieveCrime();
        }
    }

    File getPhotoFile(Crime crime, Context context) {
        File filesDir = context.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }
}
