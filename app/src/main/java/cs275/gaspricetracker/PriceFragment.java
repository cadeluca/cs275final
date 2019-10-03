package cs275.gaspricetracker;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.Date;
import java.util.UUID;


public class PriceFragment extends Fragment {

    private static final String ARG_PRICE_ID = "price_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private Price mPrice;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private Button mSharePriceButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private EditText mPriceInput;
    private TextView mPriceDisplay;

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

        // disabled date picker

//        mDateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v14) {
//                FragmentManager manager = PriceFragment.this.getFragmentManager();
//                DatePickerFragment dialog = DatePickerFragment
//                        .newInstance(mPrice.getDate());
//                dialog.setTargetFragment(PriceFragment.this, REQUEST_DATE);
//                dialog.show(manager, DIALOG_DATE);
//            }
//        });

        // disabled check box

//        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.price_solved);
//        mSolvedCheckbox.setChecked(mPrice.isSolved());
//        mSolvedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                mPrice.setSolved(isChecked);
//            }
//        });

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

        // disabled contact selection

//        final Intent pickContact = new Intent(Intent.ACTION_PICK,
//                ContactsContract.Contacts.CONTENT_URI);
//        mSuspectButton = (Button) v.findViewById(R.id.price_suspect);
//        mSuspectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v12) {
//                PriceFragment.this.startActivityForResult(pickContact, REQUEST_CONTACT);
//            }
//        });

//        PackageManager packageManager = getActivity().getPackageManager();
//        if (packageManager.resolveActivity(pickContact,
//                PackageManager.MATCH_DEFAULT_ONLY) == null) {
//            mSuspectButton.setEnabled(false);
//        }

        // disabled call button

//        mCallButton = (Button) v.findViewById(R.id.price_call);
//        if (mPrice.getSuspect() == null) {
//            mCallButton.setEnabled(false);
//            mCallButton.setText(R.string.call_suspect);
//        } else {
//            mCallButton.setText(getString(R.string.price_call_text, mPrice.getSuspect()));
//        }
//        mCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v1) {
//                if (mPrice.getSuspectNumber() != null) {
//                    Intent intent = new Intent(Intent.ACTION_DIAL,
//                            Uri.parse("tel:" + mPrice.getSuspectNumber()));
//                    PriceFragment.this.startActivity(intent);
//                }
//            }
//        });


        // for the price display, to make it easy I tried creating an input in which
        // the user simply types the price without having to press the decimal, but it
        // would require hiding the input field. In the meantime, I can add an input that
        // requires putting in the decimal by hand.
        mPriceDisplay = (TextView) v.findViewById(R.id.price_display);
        mPriceInput = (EditText) v.findViewById(R.id.price_input);
        mPriceInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mPriceDisplay.setText(addCurrencySign(charSequence.toString()));
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
                mSuspectButton.setText(suspect);
                mCallButton.setEnabled(true);
                mCallButton.setText(getString(R.string.price_call_text, mPrice.getSuspect()));
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

    private String addCurrencySign(String digits) {
        String amount = "$";

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
