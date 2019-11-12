package cs275.gaspricetracker;

import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;

public class GetPricesAsync extends AsyncTask<Void,String,String> {
    private ArrayList<Price> mPrices;
    private String res;
    @Override
    protected String doInBackground(Void... parmam) {
        try {
            // GET Request
            res = RequestHandler.sendGet("https://prodevsblog.com/android_get.php");
            Log.d("GETTAG", res);
            return res;
        }
        catch(Exception e){
            return "Exception: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if(s!=null){
            Log.d("getWorked", s);
        }
    }
}

