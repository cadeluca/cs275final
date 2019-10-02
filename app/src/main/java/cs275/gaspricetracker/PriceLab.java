package cs275.gaspricetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import cs275.gaspricetracker.database.CrimeBaseHelper;
import cs275.gaspricetracker.database.PriceCursorWrapper;
import cs275.gaspricetracker.database.CrimeDbSchema.CrimeTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cs275.gaspricetracker.database.CrimeDbSchema.CrimeTable.Cols.*;

public class PriceLab {
    private static PriceLab sPriceLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static PriceLab get(Context context) {
        if (sPriceLab == null) {
            sPriceLab = new PriceLab(context);
        }

        return sPriceLab;
    }

    private PriceLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext)
                .getWritableDatabase();

    }

    public void addCrime(Price p) {
        ContentValues values = getContentValues(p);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public List<Price> getPrices() {
        List<Price> prices = new ArrayList<>();
        try (PriceCursorWrapper cursor = queryCrimes(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prices.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }
        return prices;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Price getPrice(UUID id) {
        try (PriceCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPrice();
        }
    }

    public void updateCrime(Price price) {
        String uuidString = price.getId().toString();
        ContentValues values = getContentValues(price);
        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private PriceCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new PriceCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(Price price) {
        ContentValues values = new ContentValues();
        values.put(UUID, price.getId().toString());
        values.put(TITLE, price.getTitle());
        values.put(DATE, price.getDate().getTime());
        values.put(SOLVED, price.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, price.getSuspect());

        return values;
    }
}
