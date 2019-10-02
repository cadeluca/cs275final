package cs275.gaspricetracker.database;


import android.database.Cursor;
import android.database.CursorWrapper;

import cs275.gaspricetracker.Crime;
import cs275.gaspricetracker.database.CrimeDbSchema.CrimeTable;

import java.util.Date;
import java.util.UUID;

import static cs275.gaspricetracker.database.CrimeDbSchema.CrimeTable.*;

public class CrimeCursorWrapper extends CursorWrapper {

    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        long date = getLong(getColumnIndex(Cols.DATE));
        int isSolved = getInt(getColumnIndex(Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);

        return crime;
    }
}