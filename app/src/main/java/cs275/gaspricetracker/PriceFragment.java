package cs275.gaspricetracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.Date;
import java.util.UUID;


public class PriceFragment extends Fragment {

    private static final String ARG_PRICE_ID = "price_id";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private Price mPrice;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mSharePriceButton;
    private EditText mPriceInput;

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
        UUID priceId = (UUID) getArguments().getSerializable(ARG_PRICE_ID);
        mPrice = PriceLab.get(getActivity()).getPrice(priceId);
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

        mSharePriceButton = (Button) v.findViewById(R.id.price_share);
        mSharePriceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v13) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, PriceFragment.this.getPriceReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        PriceFragment.this.getString(R.string.price_report_subject));
                i = Intent.createChooser(i, PriceFragment.this.getString(R.string.send_report));
                PriceFragment.this.startActivity(i);
            }
        });


        // todo:
        //  for the price display, right now it is a straightforward text input, but I would
        //  like to add some automatic formatting for the user
        mPriceInput = (EditText) v.findViewById(R.id.price_input);
        mPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        PriceLab.get(getActivity()).updatePrice(mPrice);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mPrice.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            String suspectId;

            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                suspectId = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                mPrice.setSuspect(suspect);
            } finally {
                c.close();
            }

            contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            queryFields = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
            c = getActivity().getContentResolver()
                    .query(contactUri, queryFields,
                            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ? ",
                            new String[] {suspectId}, null);

            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                mPrice.setSuspectNumber(number);
            } finally {
                c.close();
            }
        }
    }

    private void updateDate() {
        mDateButton.setText(mPrice.getDate().toString());
    }

    private String getPriceReport() {
        String solvedString = null;
        if (mPrice.isSolved()) {
            solvedString = getString(R.string.price_report_solved);
        } else {
            solvedString = getString(R.string.price_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mPrice.getDate()).toString();
        String suspect = mPrice.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.price_report_no_suspect);
        } else {
            suspect = getString(R.string.price_report_suspect, suspect);
        }
        String report = getString(R.string.price_report,
                mPrice.getTitle(), dateString, solvedString, suspect);
        return report;
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
