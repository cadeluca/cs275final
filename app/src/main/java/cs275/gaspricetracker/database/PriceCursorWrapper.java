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

    /**
     * Retriev fields from the db and map the values to a new price
     * @return the price
     */
    public Price getPrice() {
        String uuidString = getString(getColumnIndex(Cols.UUID));
        String title = getString(getColumnIndex(Cols.TITLE));
        long date = getLong(getColumnIndex(Cols.DATE));
        float gasPrice = getFloat(getColumnIndex(Cols.PRICE));
        double latitude = getDouble(getColumnIndex(Cols.LATITUDE));
        double longitude = getDouble(getColumnIndex(Cols.LONGITUDE));
        int databaseId = getInt(getColumnIndex(Cols.DATABASE_ID));
        int hasPhoto = getInt(getColumnIndex(Cols.HAS_PHOTO));
        Price price = new Price(UUID.fromString(uuidString));
        price.setTitle(title);
        price.setDatabaseId(databaseId);
        price.setDate(new Date(date));
        price.setGasPrice(gasPrice);
        price.setLongitude(longitude);
        price.setLatitude(latitude);
        price.setHasPhoto(hasPhoto);
        return price;
    }
}