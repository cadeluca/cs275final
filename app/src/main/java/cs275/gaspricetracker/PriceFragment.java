package cs275.gaspricetracker;

import android.app.Activity;
import android.content.Entity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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

import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.HttpResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import okio.ByteString;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;


public class PriceFragment extends Fragment {

    private static final String ARG_PRICE_ID = "price_id";
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private static final String DIALOG_DELETE = "dialog_delete";
    private static final int REQUEST_CONTACT = 1;
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
    private Target saveFileTarget;
    private File FILEPATH;
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
        Log.d("postWorked", "this price id before calling async:"+mPrice.getDatabaseId());
        mPhotoFile = PriceLab.get(getActivity()).getPhotoFile(mPrice);

        // image title object
        byte[] byteImageTitle = mPrice.getPhotoFilename().getBytes();
        mEncodeImageTitle = Base64.encodeToString(byteImageTitle,Base64.DEFAULT);
        new HasImageAsync().execute(mEncodeImageTitle);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_price, container, false);
        mTitleField = (EditText) v.findViewById(R.id.price_title);
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

        mDateButton = v.findViewById(R.id.price_date);
        mDateButton.setText(mPrice.getDate().toString());
        updateDate();

        mReadLocationView = (TextView) v.findViewById(R.id.readLocationView);
        mReadLocationView.setText("latitude: "+ mPrice.getLatitude() + " longitude: " + mPrice.getLongitude());

        mSharePriceButton = (Button) v.findViewById(R.id.price_share);
        mSharePriceButton.setOnClickListener(v13 -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, PriceFragment.this.getPriceReport());
            i.putExtra(Intent.EXTRA_SUBJECT,
                    PriceFragment.this.getString(R.string.price_report_subject));
            i = Intent.createChooser(i, PriceFragment.this.getString(R.string.send_report));
            PriceFragment.this.startActivity(i);
        });

        mSaveEditsButton = (Button) v.findViewById(R.id.price_edit);
        mSaveEditsButton.setOnClickListener(view -> {
            Log.d("myTag", "Clicked saved edits");
            Log.d("postWorked", "this price id before calling async:"+mPrice.getDatabaseId());
            // post to database
            new UpdatePriceAsync().execute(mPrice);

            PriceLab.get(getActivity()).updatePrice(mPrice);
            Toast toast = Toast.makeText(getContext(), "Edited price successfully!", Toast.LENGTH_SHORT);
            toast.show();
        });

        mPriceInput = (EditText) v.findViewById(R.id.price_input);
        mPriceInput.setText(Float.toString(mPrice.getGasPrice()));
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
//
        mPhotoView = (ImageView) v.findViewById(R.id.price_photo);
//        String url = "https://ywu10.w3.uvm.edu/cs008/x.jpg";
//        Picasso.get().load(url).into(mPhotoView);

        updatePhotoView();

        mPhotoView.setOnClickListener(view -> {
            if (mPhotoFile != null && mPhotoFile.exists()) {
                FragmentManager manager = getFragmentManager();
                ImageViewFragment dialog = ImageViewFragment.newInstance(mPrice.getPhotoFilename());
                dialog.show(manager, DIALOG_PHOTO);
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("myTag", "on pause called");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "cs275.gaspricetracker.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            // upload took image to server
            // encode image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.inPurgeable = true;
            Bitmap bm = BitmapFactory.decodeFile(mPhotoFile.getPath(),options);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);

            // bitmap object
            byte[] byteImagePhoto = baos.toByteArray();
            String encodedImage = Base64.encodeToString(byteImagePhoto,Base64.DEFAULT);

            //send encode string to server
            new ImageUploadAsync().execute(encodedImage, mEncodeImageTitle);

            updatePhotoView();
        } else if (requestCode == REQUEST_DELETE) {
            new DeletePriceAsync().execute(mPrice);
            new DeleteImageAsync().execute(mEncodeImageTitle);
            PriceLab.get(getActivity()).deletePrice(mPrice);
            getActivity().finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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

    private String getPriceReport() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd");
        String dateString = simpleDateFormat.format(mPrice.getDate());
        String report = getString(R.string.price_report);
        String priceString = String.format("$%.2f", mPrice.getGasPrice());
        return String.format(report, mPrice.getTitle(), priceString, dateString);
    }

    private void updatePhotoView() {
        if (mHasImage) {
            String url = "https://jtan5.w3.uvm.edu/cs275/" + mPrice.getPhotoFilename();
            Picasso.get().load(url).into(mPhotoView);
        } else {
            //default image from server
            String url1 = "https://jtan5.w3.uvm.edu/cs275/default.jpg";
            Picasso.get().load(url1).into(mPhotoView);
        }


//            // local image take by camera
//            Bitmap bitmap = PictureUtils.getScaledBitmap(
//                    mPhotoFile.getPath(),getActivity());
//
//            mPhotoView.setImageBitmap(bitmap);
//            mPrice.setHasPhoto(1);

        // Show image from server
//            Target target = new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    mPhotoView.setImageBitmap(bitmap);
//                }
//
//                @Override
//                public void onBitmapFailed(Exception e, Drawable d) {
//
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable d) {
//
//                }
//            };
//            Picasso.get().load("https://jtan5.w3.uvm.edu/cs008/Junda.jpg").into(target);

    }

//    private void updatePhotoFile() {

//    }


//    private void updatePhotoFile() {
//        Picasso.get().load("https://ywu10.w3.uvm.edu/cs008/x.jpg").into(saveFileTarget);
//
//        saveFileTarget = new Target() {
//            @Override
//            public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from) {
//                new Thread(new Runable() {
//                    @Override
//                    public void run() {
//                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + FILEPATH);
//                        try{
//                            file.createNewFile();
//                            FileOutputStream ostream = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
//                            ostream.close();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                }).start();
//            }
//        }
//    }
//
//

    private static class UpdatePriceAsync extends AsyncTask<Price,String,String> {

        @Override
        protected String doInBackground(Price... param) {
            Price price1 = param[0];
            int id = price1.getDatabaseId();
            String title = price1.getTitle();
            float price = price1.getGasPrice();
            double longitude = price1.getLongitude();
            double latitude = price1.getLatitude();
            Log.d("postWorked", "this price's db id: "+id);
            try {
                // put the values for the POST Request
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                postDataParams.put("title", title);
                postDataParams.put("price", price);
                postDataParams.put("longitude", longitude);
                postDataParams.put("latitude", latitude);

                return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/update.php", postDataParams);
            }
            catch(Exception e){

                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s!=null){
                Log.d("updateWorked", s);
            }
        }
    }

    public static class DeletePriceAsync extends AsyncTask<Price,String,String> {

        @Override
        protected String doInBackground(Price... param) {
            Price price1 = param[0];
            int id = price1.getDatabaseId();
            try {
                // delete the id
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("id", id);
                return RequestHandler.sendPost("https://cadeluca.w3.uvm.edu/gasPriceTrackerTest/delete.php", postDataParams);
            }
            catch(Exception e){
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }


    private class FetchItemTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                String result = new PriceFetcher().getUrlString("http://jtan5.w3.uvm.edu/cs008/Junda.jpg");
                Log.i("URL","Fetched contents of URL: " + result);

            } catch (IOException ioe) {
                Log.e("URL", "Failed to fetch URL" + ioe);
            }
            return null;
        }
    }

    private class ImageUploadAsync extends  AsyncTask<String, Void, Void> {

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
                Log.i("URL", ""+ response);
            } catch (UnsupportedEncodingException e) {
                Log.i("upload URL",""+e);

            } catch (ClientProtocolException e) {
                Log.i("upload URL",""+e);
            } catch (IOException e) {
                Log.i("upload URL", ""+e);
            }
            return null;
        }
    }

    private class DeleteImageAsync extends  AsyncTask<String, Void, Void> {

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
                Log.i("URL", ""+ response);
            } catch (UnsupportedEncodingException e) {
                Log.i("upload URL",""+e);

            } catch (ClientProtocolException e) {
                Log.i("upload URL",""+e);
            } catch (IOException e) {
                Log.i("upload URL", ""+e);
            }
            return null;
        }
    }

    private class HasImageAsync extends  AsyncTask<String, Void, Void> {
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
                Log.i("URL", "uploaded response: "+ response);
            } catch (UnsupportedEncodingException e) {
                Log.i("upload URL","unsopported "+e);

            } catch (ClientProtocolException e) {
                Log.i("upload URL","client protocol "+e);
            } catch (IOException e) {
                mHasImage = false;
                Log.i("upload URL", "ioe "+e);
            }
            return null;
        }
    }


    /**
     * Takes in an input string and coverts it to a $_.__ formatted string
     * @param digits the string (from charSequence) from a view/edit
     * @return formatted string
     */
    private String addCurrencySign(String digits) {
        String amount = "$";
        // remove any non numeric chars
        digits = digits.replace(".", "");

        // Amount length greater than 2 means we need to add a decimal point
        if (digits.length() > 2) {
            String dollar = digits.substring(0, digits.length() - 2); // Pound part
            String cents = digits.substring(digits.length() - 2); // Pence part
            amount += dollar + "." + cents;
        }
        else if (digits.length() == 1) {
            amount += "0.0" + digits;
        }
        else if (digits.length() == 2) {
            amount += "0." + digits;
        }

        return amount;
    }

}
