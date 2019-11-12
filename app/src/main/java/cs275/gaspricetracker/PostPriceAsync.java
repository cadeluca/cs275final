package cs275.gaspricetracker;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

public class PostPriceAsync extends AsyncTask<Price,String,String> {
    private Price mPrice;
    private String title;
    private Float price;
    private int uID;
    @Override
    protected String doInBackground(Price... parmam) {
        mPrice = parmam[0];

        title = mPrice.getTitle();
        price = mPrice.getGasPrice();
        // todo: add the user id flag, need to figure out how to store
        //  the uuid in the db since a uuid is too big for an int
//        uID = Integer.parseInt("" + mPrice.getId().toString().charAt(0));
        uID = 1;
        try {
            // todo:
            //  move this to a new async class
            //GET Request
            //return RequestHandler.sendGet("https://prodevsblog.com/android_get.php");


            // POST Request
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("title", title);
            postDataParams.put("price", price);
            postDataParams.put("uID", uID);

            return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php",postDataParams);
        }
        catch(Exception e){
            return "Exception: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if(s!=null){
            Log.d("postWorked", s);
        }
    }
}

/**
private class SearchTask extends AsyncTask<Location, Void, Void> {
    private GalleryItem mGalleryItem;
    private Bitmap mBitmap;
    private Location mLocation;
    @Override
    protected Void doInBackground(Location... param) {
        mLocation = param[0];
        PriceFetcher fetchr = new PriceFetcher();
        List<GalleryItem> items = fetchr.searchPhotos(param[0]);
        if (items.size() == 0) {
            return null;
        }
        mGalleryItem = items.get(0);
        try {
            byte[] bytes = fetchr.getUrlBytes((mGalleryItem.getmUrl()));
            mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        } catch (IOException ioe) {
            Log.i(TAG, "Unable to download bitmap", ioe);
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        mMapImage = mBitmap;
        mMapItem = mGalleryItem;
        mCurrentLocation = mLocation;

        updateUI();
    }
}
 **/