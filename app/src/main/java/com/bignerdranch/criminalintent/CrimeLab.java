package com.bignerdranch.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bignerdranch.criminalintent.database.CrimeCursorWrapper;
import com.bignerdranch.criminalintent.database.CrimeDbHelper;
import com.bignerdranch.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: supposedly this class is a Singleton because it centralizes control of this list. But this
// particular singleton simply returns the list, that then ANYONE can modify. I'm leaving it as is for
// now until I'm sure it's not needed in later chapters for some reason.
class CrimeLab {

    private static CrimeLab sCrimeLab;

    private Context mContext;   // SOS: stored cause we'll use this in chapter16
    private SQLiteDatabase mDb;

    private CrimeLab(Context context) {
        // SOS: we use the context of the application, because it has the same lifetime as the static
        // CrimeLab instance. Whereas, activities come and go...
        mContext = context.getApplicationContext();
        mDb = new CrimeDbHelper(mContext).getWritableDatabase();
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
        // SOS: the changed crime still has the same UUID, so we use that to identify its row in the
        // db. Also, note that whereClause is split in 2 args to protect against SQL injection attack.
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

        // SOS: 'try-with-resources' will call cursor.close() even if exception happens
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

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        // SOS: SQLite can only store NULL, INTEGER, REAL, TEXT, BLOB. So we use getTime() to store
        // the date as the ms elapsed since 1/1/1970 and we store 0 or 1 for the boolean
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);

        return values;
    }
}
