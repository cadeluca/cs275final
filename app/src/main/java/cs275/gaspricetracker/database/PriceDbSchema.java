package cs275.gaspricetracker.database;

public class PriceDbSchema {
    public static final class PriceTable {
        public static final String NAME = "price";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String PRICE = "price";
        }
    }
}
