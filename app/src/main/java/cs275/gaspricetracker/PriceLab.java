package cs275.gaspricetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cs275.gaspricetracker.database.PriceBaseHelper;
import cs275.gaspricetracker.database.PriceCursorWrapper;
import cs275.gaspricetracker.database.PriceDbSchema;
import cs275.gaspricetracker.database.PriceDbSchema.PriceTable;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static cs275.gaspricetracker.database.PriceDbSchema.PriceTable.Cols.*;

/**
 * Handler class for storing prices and performing all actions (queries, CRUD ops, displaying, etc.)
 */
public class PriceLab {
    private static PriceLab sPriceLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private PriceBaseHelper mHelper;

    public static PriceLab get(Context context) {
        if (sPriceLab == null) {
            sPriceLab = new PriceLab(context);
        }

        return sPriceLab;
    }

    /**
     * Create a new PriceLab that will live throughout the app
     *
     * @param context application context
     */
    private PriceLab(Context context) {
        mContext = context.getApplicationContext();
        mHelper = new PriceBaseHelper(mContext);
        mDatabase = mHelper.getWritableDatabase();
        // Launch async get db task on first creation
        new GetPricesAsync().execute();
    }

    /**
     * Add price to local db
     *
     * @param p price to add
     */
    public void addPrice(Price p) {
        ContentValues values = getContentValues(p);
        mDatabase.insert(PriceTable.NAME, null, values);
    }

    /**
     * Delete price from local db
     *
     * @param p price to delete
     */
    public void deletePrice(Price p) {
        mDatabase.delete(
                PriceDbSchema.PriceTable.NAME,
                PriceDbSchema.PriceTable.Cols.UUID + " = ?",
                new String[]{p.getId().toString()}
        );
    }


    /**
     * Get all prices stored in the local db
     *
     * @return list collection of price
     */
    public List<Price> getPrices() {
        List<Price> prices = new ArrayList<>();
        try (PriceCursorWrapper cursor = queryPrices(null, null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prices.add(cursor.getPrice());
                cursor.moveToNext();
            }
        }
        return prices;
    }

    /**
     * @param sort sort type; we only have one additional sort type after standard
     *             but others could be set as constants and used
     * @return list collection of price
     */
    // get a List of sorted prices.
    public List<Price> getPrices(String sort) {
        List<Price> prices = new ArrayList<>();
        try (PriceCursorWrapper cursor = queryPrices(null, null, sort)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prices.add(cursor.getPrice());
                cursor.moveToNext();
            }
        }
        return prices;
    }

    /**
     * Retrieve price by UUID from lcoal db
     *
     * @param id UUID of price
     * @return the price
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Price getPrice(UUID id) {
        try (PriceCursorWrapper cursor = queryPrices(
                PriceTable.Cols.UUID + " = ?",
                new String[]{id.toString()}, ""
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPrice();
        }
    }

    /**
     * Retrieve attached photo per price
     *
     * @param price price
     * @return new file from photo file path and name
     */
    public File getPhotoFile(Price price) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, price.getPhotoFilename());
    }

    /**
     * Update price in local db
     *
     * @param price price to add
     */
    public void updatePrice(Price price) {
        String uuidString = price.getId().toString();
        ContentValues values = getContentValues(price);
        mDatabase.update(PriceTable.NAME, values,
                PriceTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    /**
     * @param whereClause optional where clause
     * @param whereArgs   optional where args
     * @param sort        optional sort type; we only have only one additional type,
     *                    if not given sort is default
     * @return PriceCursorWrapper with cursor
     */
    private PriceCursorWrapper queryPrices(String whereClause, String[] whereArgs, String sort) {
        Cursor cursor;
        // sort by price
        if (sort == "price") {
            cursor = mDatabase.query(
                    PriceTable.NAME,
                    null, // Columns - null selects all columns
                    whereClause,
                    whereArgs,
                    null, // groupBy
                    null, // having
                    PRICE
            );
        }
        // default list order
        else {
            cursor = mDatabase.query(
                    PriceTable.NAME,
                    null, // Columns - null selects all columns
                    whereClause,
                    whereArgs,
                    null, // groupBy
                    null, // having
                    null
            );
        }
        return new PriceCursorWrapper(cursor);
    }

    /**
     * Sync the lcoal db form remote db
     *
     * @param prices prices to sync
     */
    public void syncPrices(ArrayList<Price> prices) {
        mHelper.syncDB(mDatabase);
        for (Price p : prices) {
            ContentValues values = getContentValues(p);
            mDatabase.insert(PriceTable.NAME, null, values);
        }
    }

    /**
     * @param whereClause optional where clause
     * @param whereArgs   optional where args
     * @return PriceCursorWrapper with cursor
     */
    private PriceCursorWrapper queryPrices(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                PriceTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy   // Tablename.cols.
        );
        return new PriceCursorWrapper(cursor);
    }

    /**
     * Fetch content values of price
     * @param price price to grab from
     * @return ContentValues of price
     */
    private static ContentValues getContentValues(Price price) {
        ContentValues values = new ContentValues();
        values.put(UUID, price.getId().toString());
        values.put(TITLE, price.getTitle());
        values.put(DATABASE_ID, price.getDatabaseId());
        values.put(DATE, price.getDate().getTime());
        values.put(PRICE, price.getGasPrice());
        values.put(LATITUDE, price.getLatitude());
        values.put(LONGITUDE, price.getLongitude());
        values.put(HAS_PHOTO, price.getHasPhoto());
        return values;
    }

    /**
     * Async task to retrieve all prices from remote db
     */
    public class GetPricesAsync extends AsyncTask<Void, String, String> {
        private String res;

        @Override
        protected String doInBackground(Void... param) {
            try {
                // GET Request
                res = RequestHandler.sendGet("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php");
                Log.d("getDb", res);
                return res;
            } catch (Exception e) {
                return "GET Exception: " + e.getMessage();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                ArrayList<Price> prices = new ArrayList<>();
                final JSONArray[] priceArray = new JSONArray[1];
                try {
                    JSONObject j = new JSONObject(s);
                    j.keys().forEachRemaining(key -> {
                        Object value = null;
                        try {
                            value = j.get(key);
                            priceArray[0] = (JSONArray) value;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String val = ("Key: " + key + " Value: " + value);
                        Log.d("GET_PRICE_JSON", val);

                    });

                    // parse the JSON; for reach item create a price, set its values,
                    // and load the prices into the PriceLab singleton for
                    for (int n = 0; n < priceArray[0].length(); n++) {
                        JSONArray subArr = (JSONArray) priceArray[0].get(n);
                        Price p = new Price();
                        p.setDatabaseId(Integer.parseInt(subArr.get(0).toString()));
                        p.setTitle(subArr.get(1).toString());
                        p.setGasPrice(Float.parseFloat(subArr.get(2).toString()));
                        Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(subArr.get(3).toString());
                        p.setDate(date);
                        p.setLongitude(Double.parseDouble(subArr.get(4).toString()));
                        p.setLatitude(Double.parseDouble(subArr.get(5).toString()));
                        prices.add(p);
                    }

                } catch (JSONException err) {
                    Log.d("SYNC Error", err.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // begin the sync with the new loaded data
                syncPrices(prices);
            }
        }
    }
}
