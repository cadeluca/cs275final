package cs275.gaspricetracker;

import java.util.Date;
import java.util.UUID;

public class Price {

    private UUID mId;
    private int mDatabaseId;
    private String mTitle;
    private Date mDate;

    private double mLatitude;
    private double mLongitude;
    private float mGasPrice;

    private boolean mHasPhoto;

    /**
     * Random UUID generated price constructor
     */
    public Price() {
        this(UUID.randomUUID());
    }


    /**
     * Create a price given a UUID and initialize with today's date
     *
     * @param id UUID for price
     */
    public Price(UUID id) {
        mId = id;
        mDate = new Date();
    }

    /**
     * @return price UUID
     */
    public UUID getId() {
        return mId;
    }

    /**
     * @return title of gas station of price
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * @param title new gas station title
     */
    public void setTitle(String title) {
        mTitle = title;
    }

    /**
     * @return get the date the price was reported
     */
    public Date getDate() {
        return mDate;
    }

    /**
     * @param date new report date
     */
    public void setDate(Date date) {
        mDate = date;
    }

    /**
     * @return latitude coordinate of price
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * @param latitude new lat coordinate of price
     */
    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    /**
     * @param longitude new long coordinate of price
     */
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    /**
     * @return longitude coordinate of price
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * @return non-formatted price value
     */
    public float getGasPrice() {
        return mGasPrice;
    }

    /**
     * @param gasPrice new price value
     */
    public void setGasPrice(float gasPrice) {
        this.mGasPrice = gasPrice;
    }

    public String getPhotoFilename2() {
        return "IMG_" + getDatabaseId() + ".jpg";
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    /**
     * retrieve the new id stored in the SQL database for the price upon posting
     *
     * @param dbId unique id
     */
    public void setDatabaseId(int dbId) {
        this.mDatabaseId = dbId;
    }

    /**
     * @return SQL database id for this price
     */
    public int getDatabaseId() {
        return this.mDatabaseId;
    }

    /**
     * @return bool of attached photo
     */
    public boolean getHasPhoto() {
        return mHasPhoto;
    }

    /**
     * @param hasPhoto bool if photo present given integer code for stored photo
     */
    public void setHasPhoto(Integer hasPhoto) {
        if (hasPhoto == 1)
            mHasPhoto = true;
        else if (hasPhoto == 0)
            mHasPhoto = false;
        else
            mHasPhoto = false;
    }
}
