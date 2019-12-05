package cs275.gaspricetracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class PriceFragment extends Fragment {

    private static final String ARG_PRICE_ID = "price_id";
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private static final String DIALOG_DELETE = "dialog_delete";
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_DELETE = 3;
    private Price mPrice;
    private File mPhotoFile;
    private EditText mTitleField;
    private TextView mDateButton;
    private Button mSharePriceButton;
    private Button mSaveEditsButton;
    private EditText mPriceInput;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private TextView mReadLocationView;
    private String mEncodeImageTitle;
    private boolean mHasImage;

    public static PriceFragment newInstance(UUID priceId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRICE_ID, priceId);
        PriceFragment fragment = new PriceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UUID priceId = (UUID) getArguments().getSerializable(ARG_PRICE_ID);
        mPrice = PriceLab.get(getActivity()).getPrice(priceId);
        mPhotoFile = PriceLab.get(getActivity()).getPhotoFile(mPrice);

        // image title object
        byte[] byteImageTitle = mPrice.getPhotoFilename2().getBytes();
        mEncodeImageTitle = Base64.encodeToString(byteImageTitle, Base64.DEFAULT);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_price, container, false);
        mTitleField = v.findViewById(R.id.price_title);
        mTitleField.setText(mPrice.getTitle());
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

        // formatting for price, long, and lat
        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);

        // date reported
        mDateButton = v.findViewById(R.id.price_date);
        mDateButton.setText(mPrice.getDate().toString());
        updateDate();

        // format and display the long and lat
        mReadLocationView = v.findViewById(R.id.readLocationView);
        String locationStr = "latitude: " + df.format(mPrice.getLatitude()) + " longitude: " + df.format(mPrice.getLongitude());
        mReadLocationView.setText(locationStr);

        // share functionality if you want to let friends know about the price
        mSharePriceButton = v.findViewById(R.id.price_share);
        mSharePriceButton.setOnClickListener(v13 -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, PriceFragment.this.getPriceReport());
            i.putExtra(Intent.EXTRA_SUBJECT,
                    PriceFragment.this.getString(R.string.price_report_subject));
            i = Intent.createChooser(i, PriceFragment.this.getString(R.string.send_report));
            PriceFragment.this.startActivity(i);
        });

        // save edits is how you commit your changes when editing a price; otherwise edits are abandoned
        mSaveEditsButton = v.findViewById(R.id.price_edit);
        mSaveEditsButton.setOnClickListener(view -> {
            Log.d("EDIT_PRICE", "Clicked saved edits");
            // post update to database
            new UpdatePriceAsync().execute(mPrice);
            PriceLab.get(getActivity()).updatePrice(mPrice);
            Toast toast;
            toast = Toast.makeText(getContext(), R.string.edit_price_success, Toast.LENGTH_SHORT);
            toast.show();
        });

        // price value
        mPriceInput = v.findViewById(R.id.price_input);
        mPriceInput.setText(df.format(mPrice.getGasPrice()));
        mPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPrice.setGasPrice(Float.parseFloat(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        PackageManager packageManager = Objects.requireNonNull(getActivity()).getPackageManager();

        // photo optional functionality; request and capture image
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        // taking a photo
        if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getActivity()),
                    "cs275.gaspricetracker.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // upload took image to server; encode image
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
            if (!mPrice.getPhotoFilename2().equals("IMG_0.jpg")) {
                new ImageUploadAsync().execute(encodedImage, mEncodeImageTitle);
            }
            updatePhotoView();
        } else if (requestCode == REQUEST_DELETE) {
            // delete the image and the price
            new DeletePriceAsync().execute(mPrice);
            new DeleteImageAsync().execute(mEncodeImageTitle);
            PriceLab.get(getActivity()).deletePrice(mPrice);
            Objects.requireNonNull(getActivity()).finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // deleting the price with confirmation dialog
            case R.id.delete_price:
                FragmentManager manager = getFragmentManager();
                DeleteDialogFragment dialog = new DeleteDialogFragment();
                dialog.setTargetFragment(PriceFragment.this, REQUEST_DELETE);
                dialog.show(manager, DIALOG_DELETE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * update the date output using data formatting
     */
    private void updateDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd");
        String dateString = simpleDateFormat.format(mPrice.getDate());
        mDateButton.setText(dateString);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_price, menu);
    }

    /**
     * @return the price report formatted string with our price info
     */
    private String getPriceReport() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd");
        String dateString = simpleDateFormat.format(mPrice.getDate());
        String report = getString(R.string.price_report);
        String priceString = String.format("$%.2f", mPrice.getGasPrice());
        return String.format(report, mPrice.getTitle(), priceString, dateString);
    }

    /**
     * upload or update the image of this price if id alreayd present
     */
    private void updatePhotoView() {
        new HasImageAsync().execute(mEncodeImageTitle);
        Bitmap bitmap = PictureUtils.getScaledBitmap(
                mPhotoFile.getPath(), getActivity());
        mPhotoView.setImageBitmap(bitmap);
        if (mHasImage) {
            String url = "https://jtan5.w3.uvm.edu/cs275/" + mPrice.getPhotoFilename2();
            Picasso.get().load(url).into(mPhotoView);
            Log.i("123", "1");
        }
        if (!mHasImage) {
            String url = "https://jtan5.w3.uvm.edu/cs275/default.jpg";
            Picasso.get().load(url).into(mPhotoView);
            Log.i("123", "2");
        }
    }


    /**
     * PUT request AsyncTask using database price id to update a price in SQL db given new data
     */
    private static class UpdatePriceAsync extends AsyncTask<Price, String, String> {

        @Override
        protected String doInBackground(Price... param) {
            Price price1 = param[0];
            int id = price1.getDatabaseId();
            String title = price1.getTitle();
            float price = price1.getGasPrice();
            double longitude = price1.getLongitude();
            double latitude = price1.getLatitude();
            try {
                // put the values for the PUT Request
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                postDataParams.put("title", title);
                postDataParams.put("price", price);
                postDataParams.put("longitude", longitude);
                postDataParams.put("latitude", latitude);

                return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/update.php", postDataParams);
            } catch (Exception e) {
                return "PUT Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                Log.d("PUT update successful", s);
            }
        }
    }

    /**
     * Delete the price using the price database id through DELETE with AsyncTask
     */
    public static class DeletePriceAsync extends AsyncTask<Price, String, String> {

        @Override
        protected String doInBackground(Price... param) {
            Price price1 = param[0];
            int id = price1.getDatabaseId();
            try {
                // load the id to delete
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/delete.php", postDataParams);
            } catch (Exception e) {
                return "DELETE Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }


    /**
     * Async task to retrieve URL content; part of progress towards image handling but currently unused
     */
    private class FetchItemTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new PriceFetcher().getUrlString("http://jtan5.w3.uvm.edu/cs008/Junda.jpg");
                Log.i("URL", "Fetched contents of URL: " + result);

            } catch (IOException ioe) {
                Log.e("URL", "Failed to fetch URL" + ioe);
            }
            return null;
        }
    }

    /**
     * Async task to upload our bitmap image to Junda's server storage with image and title pair
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

    /**
     * Async task to delete image from Junda's server storage
     */
    private class DeleteImageAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... param) {
            String encodedImageTitle = param[0];
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://jtan5.w3.uvm.edu/cs275/uploadImage.php");
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("delete", encodedImageTitle));
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

    /**
     * Async task to check for image presence
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
