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
                Intent intent = new Intent(getActivity(), PriceListActivity.class);
                startActivity(intent);
            }
        });


        // todo:
        //  Report Button should launch the report activity
        mReportButton = (Button) v.findViewById(R.id.report_prices);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v13) {
//                Intent intent = new Intent(getActivity(), PriceActivity.class);
//                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
