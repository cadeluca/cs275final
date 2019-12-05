package cs275.gaspricetracker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class NewPriceFragment extends Fragment implements AsyncResponse {
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private static final String TAG = "NewLocatrFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private static final int REQUEST_PHOTO = 1;

    private PostPriceAsync mPostPriceAsync = new PostPriceAsync();
    private Button mSavePriceButton;
    private Price mPrice;
    private EditText mTitleField;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private String mEncodeImageTitle;
    private EditText mPriceInput;
    private TextView mLocationView;
    private GoogleApiClient mClient;
    private Location mCurrentLocation;
    private boolean mHasImage;

    /**
     * For this course, we are providing an offset of coordinates since the emulator location
     * is the same (not using the walker); get a different random position for each price
     *
     * @return offset double
     */
    private double getOffset() {
        Random r = new Random();
        int negator = r.nextInt(100);
        double offset;
        if (negator > 49) {
            offset = -1 * r.nextInt(100) * 0.0001;
        }
        offset = r.nextInt(100) * 0.0001;
        return offset;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        // Photo request
        if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getActivity()),
                    "cs275.gaspricetracker.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // upload took image to server and encode image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.inPurgeable = true;
            Bitmap bm = BitmapFactory.decodeFile(mPhotoFile.getPath(), options);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);

            // bitmap object
            byte[] byteImagePhoto = baos.toByteArray();
            String encodedImage = Base64.encodeToString(byteImagePhoto, Base64.DEFAULT);

            // send encode string to server
            new ImageUploadAsync().execute(encodedImage, mEncodeImageTitle);
            updatePhotoView();
        }
    }


    public static NewPriceFragment newInstance() {
        return new NewPriceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // attach our PostPriceAsync to this fragment
        mPostPriceAsync.delegate = this;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Objects.requireNonNull(Objects.requireNonNull(activity).getSupportActionBar()).setTitle(getString(R.string.new_price_fragment_header));
        mPrice = new Price();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPhotoFile = PriceLab.get(getActivity()).getPhotoFile(mPrice);

        // image title object
        byte[] byteImageTitle = mPrice.getPhotoFilename2().getBytes();
        mEncodeImageTitle = Base64.encodeToString(byteImageTitle, Base64.DEFAULT);

        // maps api client
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_price, container, false);
        mTitleField = v.findViewById(R.id.price_title);
        // only need the onTextChanged
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPrice.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSavePriceButton = v.findViewById(R.id.price_save);
        mSavePriceButton.setOnClickListener(view -> {
            // post to database and add to price lab
            mPostPriceAsync.execute(mPrice);
            PriceLab.get(getActivity()).addPrice(mPrice);

            // display success and return to list
            Toast toast = Toast.makeText(getContext(), R.string.added_price_success, Toast.LENGTH_SHORT);
            toast.show();
            Objects.requireNonNull(getActivity()).finish();
        });

        mPriceInput = v.findViewById(R.id.price_input);
        // only need the onTextChanged
        mPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    mPrice.setGasPrice(Float.parseFloat(charSequence.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // text for location string
        mLocationView = v.findViewById(R.id.newLocationView);
        String longLat = "latitude: " + mPrice.getLatitude() + " longitude: " + mPrice.getLongitude();
        mLocationView.setText(longLat);
        PackageManager packageManager = Objects.requireNonNull(getActivity()).getPackageManager();

        // Provide photo option
        mPhotoButton = v.findViewById(R.id.price_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(v1 -> {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "cs275.gaspricetracker.fileprovider",
                    mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            List<ResolveInfo> cameraActivities = getActivity()
                    .getPackageManager().queryIntentActivities(captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo activity : cameraActivities) {
                getActivity().grantUriPermission(activity.activityInfo.packageName,
                        uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(captureImage, REQUEST_PHOTO);
        });

        mPhotoView = v.findViewById(R.id.price_photo);
        updatePhotoView();

        mPhotoView.setOnClickListener(view -> {
            if (mHasImage) {
                FragmentManager manager = getFragmentManager();
                ImageViewFragment dialog = ImageViewFragment.newInstance(mPrice.getPhotoFilename2(), "downloadImage");
                dialog.show(manager, DIALOG_PHOTO);
            } else {
                FragmentManager manager = getFragmentManager();
                ImageViewFragment dialog = ImageViewFragment.newInstance(mPhotoFile.getPath(), "local");
                dialog.show(manager, DIALOG_PHOTO);
            }
        });

        return v;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), Objects.requireNonNull(getActivity()));
            mPhotoView.setImageBitmap(bitmap);
        }
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
        // set the price location on click of this button
        if (item.getItemId() == R.id.action_locate) {
            if (hasLocationPermission()) {
                findImage();
            } else {
                requestPermissions(LOCATION_PERMISSIONS,
                        REQUEST_LOCATION_PERMISSIONS);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
     * Submit a location high accuracy request to then set the long and lat values for the price
     */
    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, location -> {
                    mCurrentLocation = location;
                    Log.d(TAG, "Got a fix: " + location);
                    // using the adding offset here
                    mPrice.setLatitude(mCurrentLocation.getLatitude() + getOffset());
                    mPrice.setLongitude(mCurrentLocation.getLongitude() + getOffset());
                });
    }

    @Override
    public void processFinish(String output) {
        try {
            JSONObject jo = new JSONObject(output);
            int thisId = jo.getInt("thisId");
            Log.d("postWorked", "price id before set: " + mPrice.getDatabaseId());
            mPrice.setDatabaseId(thisId);
            Log.d("postWorked", "price id after set to make sure current price has " +
                    "the id in ref: " + mPrice.getDatabaseId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Async POST task that submits the singular created price to the database
     */
    private static class PostPriceAsync extends AsyncTask<Price, String, String> {
        // start with a null delegate
        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(Price... param) {
            Price price1 = param[0];
            String title = price1.getTitle();
            float price = price1.getGasPrice();
            double longitude = price1.getLongitude();
            double latitude = price1.getLatitude();

            try {
                // load the values for the POST request into params
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("title", title);
                postDataParams.put("price", price);
                postDataParams.put("longitude", longitude);
                postDataParams.put("latitude", latitude);
//                postDataParams.put("uuid", "1");

                // call through request handler
                return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php", postDataParams);
            } catch (Exception e) {
                return "POST Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.d("POST_PRICE_ASYNC", s);
                delegate.processFinish(s);
            }
        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (hasLocationPermission()) {
                findImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * Upload image to server Async task with image and title pair values
     */
    private class ImageUploadAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... param) {
            String encodedImage = param[0];
            String encodedImageTitle = param[1];
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://jtan5.w3.uvm.edu/cs275/uploadImage.php");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();

            pairs.add(new BasicNameValuePair("image", encodedImage));
            pairs.add(new BasicNameValuePair("title", encodedImageTitle));
            try {
                post.setEntity(new UrlEncodedFormEntity(pairs));
                org.apache.http.HttpResponse response = client.execute(post);
                Log.d("URL", "" + response);
            } catch (UnsupportedEncodingException e) {
                Log.d("upload URL", "" + e);
            } catch (ClientProtocolException e) {
                Log.d("upload URL", "" + e);
            } catch (IOException e) {
                Log.d("upload URL", "" + e);
            }
            return null;
        }
    }
}