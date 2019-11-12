package cs275.gaspricetracker;

import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


// todo: if i determine that i cannot get this modified version to work, i will delete this one.
//  Note: this is a copy of PriceFetchr, which still works and will remain.

public class PriceBackendFetcher {
    private static final String TAG = "PriceBackendFetcher";
    private static final String FETCH_ALL = "getAll";
    private static final String POST_NEW = "postNew";
    private static final Uri ENDPOINT = Uri.parse("http://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php")
            .buildUpon()
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .build();

    public void postTest(){
        URL url = null;
        try {
            url = new URL("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            con.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        String jsonInputString = "{\"title\": \"Title from App\", \"price\": \"1.50\", \"uID\": \"42\"}";
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private List<GalleryItem> downloadPrices(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    private void postPrices(String url) {
        // todo: post
    }


    private void parseItems(List<GalleryItem>items, JSONObject jsonBody)
            throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setmUrl(photoJsonObject.getString("url_s"));
            item.setmLat(photoJsonObject.getDouble("latitude"));
            item.setmLon(photoJsonObject.getDouble("longitude"));
            items.add(item);
        }
    }

    private String buildUrl (String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(POST_NEW)) {
            uriBuilder.appendQueryParameter("text", query);

        }
        return uriBuilder.build().toString();
    }

    private String buildUrl(Price price) {
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method", POST_NEW)
                .appendQueryParameter("title", "" + price.getTitle())
                .appendQueryParameter("price", "" + price.getGasPrice())
                .appendQueryParameter("uID", "" + 1)
                .build().toString();
    }


    // todo: double check how the input is and make sure they match

    public List<GalleryItem> fetchAllPrices() {
        String url = buildUrl(FETCH_ALL, null);
        return downloadPrices(url);
    }
    public void postPrice(String query) {
        String url = buildUrl (POST_NEW, query);
//        return downloadPrices(url);
    }

    public void postPrice(Price price) {
        String url = buildUrl(price);
//        return downloadPrices(url);
    }

}

