package cs275.gaspricetracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class PriceFragment extends Fragment {

    private static final String ARG_PRICE_ID = "price_id";
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private static final String DIALOG_DELETE = "dialog_delete";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_DELETE = 3;
    private Price mPrice;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mSharePriceButton;
    private Button mSaveEditsButton;
    private EditText mPriceInput;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private TextView mReadLocationView;

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

        mDateButton = (Button) v.findViewById(R.id.price_date);
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
            // todo: add async for call update price in db
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
            updatePhotoView();
        } else if (requestCode == REQUEST_DELETE) {
            PriceLab.get(getActivity()).deletePrice(mPrice);
            // todo: add async for call delete price in db
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
        mDateButton.setText(mPrice.getDate().toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_price, menu);
    }

    private String getPriceReport() {
        String solvedString = null;
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mPrice.getDate()).toString();
        String report = "temporary report string";
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
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