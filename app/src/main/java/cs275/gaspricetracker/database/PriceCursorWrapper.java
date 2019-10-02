package cs275.gaspricetracker.database;


import android.database.Cursor;
import android.database.CursorWrapper;

import cs275.gaspricetracker.Price;
import cs275.gaspricetracker.database.PriceDbSchema.PriceTable;

import java.sql.Wrapper;
import java.util.Date;
import java.util.UUID;

import static cs275.gaspricetracker.database.PriceDbSchema.PriceTable.*;

public class PriceCursorWrapper extends CursorWrapper {

    public PriceCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Price getPrice() {
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        long date = getLong(getColumnIndex(Cols.DATE));
        int isSolved = getInt(getColumnIndex(Cols.SOLVED));
        String suspect = getString(getColumnIndex(PriceTable.Cols.SUSPECT));

        Price price = new Price(UUID.fromString(uuidString));
        price.setTitle(title);
        price.setDate(new Date(date));
        price.setSolved(isSolved != 0);
        price.setSuspect(suspect);

        return price;
    }
}