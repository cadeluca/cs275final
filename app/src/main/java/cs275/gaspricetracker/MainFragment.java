package cs275.gaspricetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class MainFragment extends Fragment {

    private Button mViewButton;
    private Button mReportButton;
    private Button mPriceMapButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mPriceMapButton  = v.findViewById(R.id.view_map);
        mPriceMapButton.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), PriceLocatrActivity.class);
            startActivity(intent);
        });

        mViewButton = v.findViewById(R.id.view_prices);
        mViewButton.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), PriceListActivity.class);
            startActivity(intent);
        });

        mReportButton = v.findViewById(R.id.report_prices);
        mReportButton.setOnClickListener(v2 -> {
            Intent intent = new Intent(getContext(), NewPriceActivity.class);
            startActivity(intent);
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
