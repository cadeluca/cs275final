package cs275.gaspricetracker;

import java.util.Date;
import java.util.UUID;

public class Price {

    private UUID mId;
    private String mTitle;
    private Date mDate;

    private String mLocation;
    private float mGasPrice;


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

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
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
}
