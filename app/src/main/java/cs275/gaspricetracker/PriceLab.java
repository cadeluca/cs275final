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

    private PriceLab(Context context) {
        mContext = context.getApplicationContext();
        mHelper = new PriceBaseHelper(mContext);
        mDatabase = mHelper.getWritableDatabase();
        // Launch async get db task on first creation
        new GetPricesAsync().execute();
    }

    public void addPrice(Price p) {
        ContentValues values = getContentValues(p);
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
        try (PriceCursorWrapper cursor = queryPrices(null, null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                prices.add(cursor.getPrice());
                cursor.moveToNext();
            }
        }
        return prices;
    }

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Price getPrice(UUID id) {
        try (PriceCursorWrapper cursor = queryPrices(
                PriceTable.Cols.UUID + " = ?",
                new String[]{id.toString() }, ""
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPrice();
        }
    }

    public File getPhotoFile(Price price) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, price.getPhotoFilename());
    }

    public void updatePrice(Price price) {
        String uuidString = price.getId().toString();
        ContentValues values = getContentValues(price);
        mDatabase.update(PriceTable.NAME, values,
                PriceTable.Cols.UUID + " = ?",
                new String[]{uuidString});
        Log.d("myTag", "update price called");
    }

    private PriceCursorWrapper queryPrices(String whereClause, String[] whereArgs, String sort) {
        Cursor cursor;
        // sort in price
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
    public void syncPrices(ArrayList<Price> prices) {
        mHelper.syncDB(mDatabase);
        for (Price p : prices) {
            Log.d("postWorked", "this price id from inside syncProces:"+p.getDatabaseId());
            ContentValues values = getContentValues(p);
            // todo: consider removing these logs since we know these work
            Log.d("getDb", "add price called from sync prices called");
            mDatabase.insert(PriceTable.NAME, null, values);
        }
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
        values.put(DATABASE_ID,price.getDatabaseId());
        values.put(DATE, price.getDate().getTime());
        values.put(PRICE, price.getGasPrice());
        values.put(LATITUDE, price.getLatitude());
        values.put(LONGITUDE, price.getLongitude());
        return values;
    }

    public class GetPricesAsync extends AsyncTask<Void,String,String> {
        private String res;
        @Override
        protected String doInBackground(Void... param) {
            try {
                // GET Request
                res = RequestHandler.sendGet("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php");
                Log.d("getDb", res);
                return res;
            }
            catch(Exception e){
                return "Exception: " + e.getMessage();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String s) {
            if(s!=null){
                // todo: consider removing these logs since we know these work
                ArrayList<Price> prices = new ArrayList<>();
                final JSONArray[] priceArray = new JSONArray[1];
                try {
                    JSONObject j = new JSONObject(s);
                    j.keys().forEachRemaining(key -> {
                        Object value = null;
                        try {
                            value = j.get(key);
                            // todo: if value is null this throws an exception and crashes; it is null if the database php file encounters a db error
                            priceArray[0] = (JSONArray) value;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String val = ("Key: " + key + " Value: "+ value);
                        Log.d("jsonTest", val);

                    });

                    for(int n = 0; n < priceArray[0].length(); n++) {
                        JSONArray subArr = (JSONArray) priceArray[0].get(n);
                        Price p = new Price();
                        p.setDatabaseId(Integer.parseInt(subArr.get(0).toString()));
                        Log.d("postWorked","set db id to: "+p.getDatabaseId());
                        p.setTitle(subArr.get(1).toString());
                        p.setGasPrice(Float.parseFloat(subArr.get(2).toString()));
                        Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(subArr.get(3).toString());
                        p.setDate(date);
                        prices.add(p);
                    }

                }catch (JSONException err){
                    Log.d("Error", err.toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.d("getDb", s);
                Log.d("getDb", "start syncPrices");
                syncPrices(prices);
                Log.d("getDb", "end syncPrices");
            }
        }
    }
}
