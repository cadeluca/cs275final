package cs275.gaspricetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView;

import java.util.List;


public class PriceListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mPriceRecyclerView;
    private PriceAdapter mAdapter;
    private boolean mSubtitleVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_Price_list, container, false);

        mPriceRecyclerView = (RecyclerView) view
                .findViewById(R.id.Price_recycler_view);
        mPriceRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_Price_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_Price:
                Price Price = new Price();
                PriceLab.get(getActivity()).addPrice(Price);
                Intent intent = PricePagerActivity
                        .newIntent(getActivity(), Price.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        PriceLab PriceLab = PriceLab.get(getActivity());
        int PriceCount = PriceLab.getPrices().size();
        String subtitle = getString(R.string.subtitle_format, PriceCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private void updateUI() {
        PriceLab PriceLab = PriceLab.get(getActivity());
        List<Price> Prices = PriceLab.getPrices();

        if (mAdapter == null) {
            mAdapter = new PriceAdapter(Prices);
            mPriceRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setPrices(Prices);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    private class PriceHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Price mPrice;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        public PriceHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_Price, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.Price_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.Price_date);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.Price_solved);
        }

        public void bind(Price Price) {
            mPrice = Price;
            mTitleTextView.setText(mPrice.getTitle());
            mDateTextView.setText(mPrice.getDate().toString());
            mSolvedImageView.setVisibility(Price.isSolved() ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = PricePagerActivity.newIntent(getActivity(), mPrice.getId());
            startActivity(intent);
        }
    }

    private class PriceAdapter extends RecyclerView.Adapter<PriceHolder> {

        private List<Price> mPrices;

        public PriceAdapter(List<Price> Prices) {
            mPrices = Prices;
        }

        @Override
        public PriceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new PriceHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(PriceHolder holder, int position) {
            Price Price = mPrices.get(position);
            holder.bind(Price);
        }

        @Override
        public int getItemCount() {
            return mPrices.size();
        }

        public void setPrices(List<Price> Prices) {
            mPrices = Prices;
        }
    }
}