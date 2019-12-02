package cs275.gaspricetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cs275.gaspricetracker.database.PriceDbSchema.PriceTable;

public class PriceBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "priceBase.db";

    public PriceBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + PriceTable.NAME + "(" +
                " _id integer primary key autoincrement, " + PriceTable.Cols.UUID + ", " + PriceTable.Cols.TITLE + ", " +
                PriceTable.Cols.DATE + ", " +
                PriceTable.Cols.PRICE + ", " +
                PriceTable.Cols.LATITUDE + ", " +
                PriceTable.Cols.LONGITUDE + ", " +
                PriceTable.Cols.DATABASE_ID +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void syncDB(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + PriceTable.NAME);
        onCreate(db);
    }
}