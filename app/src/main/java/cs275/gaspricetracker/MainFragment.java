package cs275.gaspricetracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Date;
import java.util.UUID;


public class MainFragment extends Fragment {

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private Button mViewButton;
    private Button mReportButton;

    public static MainFragment newInstance() {
        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        // todo:
        //  View Button should launch the view prices activity
        mViewButton = (Button) v.findViewById(R.id.view_prices);
        mViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v14) {
//                FragmentManager manager = MainFragment.this.getFragmentManager();
//                DatePickerFragment dialog = DatePickerFragment
//                        .newInstance(mPrice.getDate());
//                dialog.setTargetFragment(MainFragment.this, REQUEST_DATE);
//                dialog.show(manager, DIALOG_DATE);
            }
        });


        // todo:
        //  Report Button should launch the report activity
        mReportButton = (Button) v.findViewById(R.id.report_prices);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v13) {
                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT, MainFragment.this.getPriceReport());
//                i.putExtra(Intent.EXTRA_SUBJECT,
//                        MainFragment.this.getString(R.string.price_report_subject));
//                i = Intent.createChooser(i, MainFragment.this.getString(R.string.send_report));
                MainFragment.this.startActivity(i);
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

}
