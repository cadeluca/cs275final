package cs275.gaspricetracker.database;

public class PriceDbSchema {
    public static final class PriceTable {
        public static final String NAME = "price";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATABASE_ID = "database_id";
            public static final String DATE = "date";
            public static final String PRICE = "price";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String HAS_PHOTO = "has_photo";
        }
    }
}
