package cs275.gaspricetracker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Objects;


public class NewPriceFragment extends Fragment {

    private static final String DIALOG_PHOTO = "DialogPhoto";

    private Button mSavePriceButton;
    private Price mPrice;
    private EditText mTitleField;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private EditText mPriceInput;

    private static final int REQUEST_PHOTO= 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle("Report New Gas Price");
        mPrice = new Price();
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
            // add the price to the PriceLab
            PriceLab.get(getActivity()).addPrice(mPrice);
            // post to database
            // todo:
            //  check if we can pull a response from the async task, for example a 200 response,
            //  so that we may conditionally issue a success or failure response.
            new PostPriceAsync().execute(mPrice);
            Toast toast = Toast.makeText(getContext(), "Added price successfully!", Toast.LENGTH_SHORT);
            toast.show();
            // go back to main
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        });

        mPriceInput = (EditText) v.findViewById(R.id.price_input);
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

        mPhotoView = (ImageView) v.findViewById(R.id.price_photo);
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

    private static class PostPriceAsync extends AsyncTask<Price,String,String> {
        private Price mPrice;
        private String title;
        private Float price;
        private Double longitude;
        private Double latitude;
        private int uID;
        @Override
        protected String doInBackground(Price... parmam) {
            mPrice = parmam[0];
            title = mPrice.getTitle();
            price = mPrice.getGasPrice();
            price = mPrice.getLongitude();
            price = mPrice.getLatitude();
            try {
                // POST Request
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("title", title);
                postDataParams.put("price", price);
                postDataParams.put("uID", uID);
                postDataParams.put("longitude", longitude);
                postDataParams.put("latitude", latitude);

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

    // todo: dont delete this yet, using this as a reference

//    private class PostTask extends AsyncTask<Price, Void, Void> {
//        private Price mPrice;
//
//        @Override
//        protected Void doInBackground(Price... param) {
//            mPrice = param[0];
//            PriceBackendFetcher fetchr = new PriceBackendFetcher();
//            fetchr.postPrice(param[0]);
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//        }
//    }
//
//    private class PostTask extends AsyncTask<Price, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Price... param) {
//            mPrice = param[0];
//            PriceBackendFetcher fetchr = new PriceBackendFetcher();
//            fetchr.postTest();
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) {
//        }
//    }

}

