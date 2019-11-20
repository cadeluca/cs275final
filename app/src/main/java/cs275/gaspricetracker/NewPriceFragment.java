package cs275.gaspricetracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class NewPriceFragment extends Fragment implements AsyncResponse {
    private static final String DIALOG_PHOTO = "DialogPhoto";
    PostPriceAsync mPostPriceAsync = new PostPriceAsync();
    private int mThisCreatedPriceId;
    private Button mSavePriceButton;
    private Price mPrice;
    private EditText mTitleField;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    // fixme: copy over from the regular price fragment all photo parts
    private File mPhotoFile;
    private EditText mPriceInput;
    private TextView mLocationView;
    private static final int REQUEST_PHOTO= 2;
    private static final String TAG = "NewLocatrFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private GoogleApiClient mClient;
    private Location mCurrentLocation;

    // testing for add offset, get a different random position for each price
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

    public static NewPriceFragment newInstance() {
        return new NewPriceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPostPriceAsync.delegate = this;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Objects.requireNonNull(Objects.requireNonNull(activity).getSupportActionBar()).setTitle("Report New Gas Price");
        mPrice = new Price();
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
            // post to database
            mPostPriceAsync.execute(mPrice);

            // add the price to the PriceLab
            PriceLab.get(getActivity()).addPrice(mPrice);

            Toast toast = Toast.makeText(getContext(), R.string.added_price_success, Toast.LENGTH_SHORT);
            toast.show();

            // go back to main
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        });
        mPriceInput = v.findViewById(R.id.price_input);
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
        mLocationView = (TextView) v.findViewById(R.id.newLocationView);
        // todo: investigate this IDE warning
        mLocationView.setText("latitude: "+ mPrice.getLatitude() + " longitude: " + mPrice.getLongitude() );
        PackageManager packageManager = getActivity().getPackageManager();

        mPhotoButton = (ImageButton) v.findViewById(R.id.price_camera);
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
            if (mPhotoFile != null && mPhotoFile.exists()) {
                FragmentManager manager = getFragmentManager();
                ImageViewFragment dialog = ImageViewFragment.newInstance(mPhotoFile.getPath());
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
        switch (item.getItemId()) {
            case R.id.action_locate :
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

    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mClient, request, location -> {
                    mCurrentLocation = location;
                    Log.i(TAG, "Got a fix: " + location);
                    // testing for adding offset
                    mPrice.setLatitude(mCurrentLocation.getLatitude() + getOffset());
                    mPrice.setLongitude(mCurrentLocation.getLongitude() + getOffset());
                });

    }

    @Override
    public void processFinish(String output) {
        try {
            JSONObject jo = new JSONObject(output);
            int thisId = jo.getInt("thisId");
            Log.d("postWorked", "price id before set: "+mPrice.getDatabaseId());
            mPrice.setDatabaseId(thisId);
            Log.d("postWorked", "price id after set: "+mPrice.getDatabaseId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static class PostPriceAsync extends AsyncTask<Price,String,String> {
        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(Price... param) {
            Price price1 = param[0];
            
            String title = price1.getTitle();
            float price = price1.getGasPrice();
            double longitude = price1.getLongitude();
            double latitude = price1.getLatitude();
            
            try {
                // put the values for the POST Request
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("title", title);
                postDataParams.put("price", price);
                postDataParams.put("longitude", longitude);
                postDataParams.put("latitude", latitude);

                return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/saveName.php", postDataParams);
            }
            catch(Exception e){

                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null){
                Log.d("postWorked", s);
                delegate.processFinish(s);
            }
        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                if(hasLocationPermission()) {
                    findImage();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}