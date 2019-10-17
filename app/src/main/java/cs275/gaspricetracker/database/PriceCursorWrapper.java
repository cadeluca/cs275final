package cs275.gaspricetracker.database;


import android.database.Cursor;
import android.database.CursorWrapper;

import cs275.gaspricetracker.Price;

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
        float gasPrice = getFloat(getColumnIndex(Cols.PRICE));

        Price price = new Price(UUID.fromString(uuidString));
        price.setTitle(title);
        price.setDate(new Date(date));
        price.setGasPrice(gasPrice);

        return price;
    }
}