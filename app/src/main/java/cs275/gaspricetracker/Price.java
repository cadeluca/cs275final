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

    public Price() {
        this(UUID.randomUUID());
    }

    public Price(UUID id) {
        mId = id;
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public double getLatitude() {
        return  mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLongitude() {
        return mLongitude;
    }


    public float getGasPrice() {
        return mGasPrice;
    }

    public void setGasPrice(float gasPrice) {
        this.mGasPrice = gasPrice;
    }

    public String getPhotoFilename() {
        return "IMG_" + getId().toString() + ".jpg";
    }

    public void setDatabaseId(int dbId) {
        this.mDatabaseId = dbId;
    }

    public int getDatabaseId() {
        return this.mDatabaseId;
    }

    public boolean getHasPhoto() {
        return mHasPhoto;
    }

    public void setHasPhoto(Integer hasPhoto) {
        if (hasPhoto == 1)
            mHasPhoto = true;
        else if (hasPhoto == 0)
            mHasPhoto = false;
        else
            mHasPhoto = false;
    }
}
