package cs275.gaspricetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import cs275.gaspricetracker.database.PriceBaseHelper;
import cs275.gaspricetracker.database.PriceCursorWrapper;
import cs275.gaspricetracker.database.PriceDbSchema;
import cs275.gaspricetracker.database.PriceDbSchema.PriceTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static cs275.gaspricetracker.database.PriceDbSchema.PriceTable.Cols.*;

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
        mDatabase = new PriceBaseHelper(mContext)
                .getWritableDatabase();

    }

    public void addPrice(Price p) {
        ContentValues values = getContentValues(p);
        // todo: change this so that there is a temporary price in memory,
        //  then if a submit button is pressed we do this
        Log.i("myTag", "add price called");
        mDatabase.insert(PriceTable.NAME, null, values);
    }

    public void deletePrice (Price p) {
        mDatabase.delete(
                PriceDbSchema.PriceTable.NAME,
                PriceDbSchema.PriceTable.Cols.UUID + " = ?",
                new String[] {p.getId().toString()}
        );
    }


    public List<Price> getPrices() {
        List<Price> prices = new ArrayList<>();
        try (PriceCursorWrapper cursor = queryPrices(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prices.add(cursor.getPrice());
                cursor.moveToNext();
            }
        }
        return prices;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Price getPrice(UUID id) {
        try (PriceCursorWrapper cursor = queryPrices(
                PriceTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPrice();
        }
    }

    public void updatePrice(Price price) {
        String uuidString = price.getId().toString();
        ContentValues values = getContentValues(price);
        mDatabase.update(PriceTable.NAME, values,
                PriceTable.Cols.UUID + " = ?",
                new String[]{uuidString});
        Log.d("myTag", "update price called");
    }

    private PriceCursorWrapper queryPrices(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                PriceTable.NAME,
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
        values.put(PriceTable.Cols.SUSPECT, price.getSuspect());

        return values;
    }
}
