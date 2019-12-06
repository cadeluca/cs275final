package cs275.gaspricetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Map fragment for displaying prices and user
 */
public class PriceLocatrFragment extends SupportMapFragment {
    private static final String TAG = "LocatrFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private GoogleApiClient mClient;
    private GoogleMap mMap;
    private Bitmap mMapImage;
    private GalleryItem mMapItem;
    private Location mCurrentLocation;
    private boolean mHasImage;

    public static PriceLocatrFragment newInstance() {
        return new PriceLocatrFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        getMapAsync(googleMap -> {
            mMap = googleMap;
            updateUI();
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);
        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                if (hasLocationPermission()) {
                    findImage();
                } else {
                    requestPermissions(LOCATION_PERMISSIONS,
                            REQUEST_LOCATION_PERMISSIONS);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    /**
     * Activate search task based on location calling on location services
     */
    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, location -> {
                    Log.i(TAG, "Got a fix: " + location);
                    new SearchTask().execute(location);
                });
    }

    /**
     * @return bool for permission check result
     */
    private boolean hasLocationPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * UI update method for setting elements (prices, user) on map
     */
    private void updateUI() {
        if (mMap == null || mMapImage == null) {
            return;
        }
        LatLng itemPoint = new LatLng(mMapItem.getmLat(),
                mMapItem.getmLon());
        Double myLatitude = mCurrentLocation.getLatitude();
        Double myLongitude = mCurrentLocation.getLongitude();

        LatLng myPoint = new LatLng(
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude()
        );

        mMap.clear();
        // Price from list
        List<Price> mPrices = PriceLab.get(getActivity()).getPrices();
        File mPhotoFile;

        if (mPrices.size() != 0) {
            for (Price mPrice : mPrices) {
                String price = "$ " + mPrice.getGasPrice();
                String price_title = mPrice.getTitle();
                mPhotoFile = PriceLab.get(getActivity()).getPhotoFile(mPrice);

                Double priceLatitude = mPrice.getLatitude();
                Double priceLongitude = mPrice.getLongitude();
                Double distance = Math.sqrt(Math.pow(priceLatitude-myLatitude, 2) + Math.pow(priceLongitude-myLongitude, 2));

                BitmapDescriptor itemBitmap;

                LatLng mPricePosition = new LatLng(mPrice.getLatitude(), mPrice.getLongitude());
                Log.i("map", mPrice.getLatitude() + "  " + mPrice.getLongitude());
                byte[] byteImageTitle = mPrice.getPhotoFilename2().getBytes();
                String encodeImageTitle = Base64.encodeToString(byteImageTitle, Base64.DEFAULT);
                new HasImageAsync().execute(encodeImageTitle);
                if (mHasImage) {
                    // get picture from server
                    Picasso.get().load("https://jtan5.w3.uvm.edu/cs275/" + mPrice.getPhotoFilename2()).into(
                            new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .anchor(0.0f, 1.0f)
                                            .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 150, 150, true)))
                                            .title(price_title)
                                            .snippet(price + " Distance from you" + String.format("%.2f",distance*111) + "km")
                                            .position(mPricePosition));
                                    marker.setTag(mPricePosition);
                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable d) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable d) {

                                }
                            });
                } else {
                    itemBitmap = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                    //  marker in blue
                    MarkerOptions itemMarker = new MarkerOptions()
                            .position(mPricePosition)
                            .icon(itemBitmap)
                            .title(price_title)
                            .snippet(price + " Distance from you " + String.format("%.2f",distance*111) + "km");
//                     add price marker to map
                    mMap.addMarker(itemMarker);
                }
            }
        }
        // My current location
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("me");


        // add marker into map
        mMap.addMarker(myMarker);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint)
                //.include(itemPoint1)
                .include(myPoint)
                .build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        //int margin = 100;
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                if (hasLocationPermission()) {
                    findImage();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Async task for search for price bitmap image
     */
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

    /**
     * Async task for image checking with encoded image title
     */
    private class HasImageAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... param) {
            mHasImage = true;
            String encodedImageTitle = param[0];
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://jtan5.w3.uvm.edu/cs275/uploadImage.php");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("hasImageTitle", encodedImageTitle));
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
                org.apache.http.HttpResponse response = client.execute(post);
                Log.d("URL", "uploaded response: " + response);
            } catch (UnsupportedEncodingException e) {
                Log.d("upload URL", "unsopported " + e);
            } catch (ClientProtocolException e) {
                Log.d("upload URL", "client protocol " + e);
            } catch (IOException e) {
                mHasImage = false;
                Log.d("upload URL", "ioe " + e);
            }
            return null;
        }
    }
}