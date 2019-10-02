package cs275.gaspricetracker;

import java.util.Date;
import java.util.UUID;

public class Price {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mNumber;

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

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getSuspectNumber() {
        return mNumber;
    }

    public void setSuspectNumber(String number) {
        mNumber = number;
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
}
