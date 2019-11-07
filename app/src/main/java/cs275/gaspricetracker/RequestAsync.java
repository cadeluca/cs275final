package cs275.gaspricetracker;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

public class RequestAsync extends AsyncTask<String,String,String> {
    @Override
    protected String doInBackground(String... strings) {
        try {
            //GET Request
            //return RequestHandler.sendGet("https://prodevsblog.com/android_get.php");

            // POST Request
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("title", "Station from Phone");
            postDataParams.put("price", "1.23");
            postDataParams.put("uID", "42");

            return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php",postDataParams);
        }
        catch(Exception e){
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String s) {
        if(s!=null){
//            Toast.makeText(G, s, Toast.LENGTH_LONG).show();
            Log.d("postWorked", s);
            Log.d("postWorked", "it worked!");
        }
    }


}
