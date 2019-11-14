//package cs275.gaspricetracker;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//public class GetPricesAsync extends AsyncTask<Void,String,String> {
//    private String res;
//    @Override
//    protected String doInBackground(Void... parmam) {
//        try {
//            // GET Request
//            res = RequestHandler.sendGet("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php");
//            Log.d("getDb", res);
//            return res;
//        }
//        catch(Exception e){
//            return "Exception: " + e.getMessage();
//        }
//    }
//
//    @Override
//    protected void onPostExecute(String s) {
//        if(s!=null){
//            Log.d("getDb", s);
//            PriceLab priceLab = PriceLab.get(getActivity());
//        }
//    }
//}
//
