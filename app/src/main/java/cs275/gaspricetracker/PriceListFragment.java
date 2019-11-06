package cs275.gaspricetracker;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;

public class PriceListFragment extends ListFragment {
    private boolean mSubtitleVisible;
    private static final String TAG = "PriceListFragment";
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private PriceAdapter mAdapter;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_price_list, menu);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mSubtitleVisible = false;
        setHasOptionsMenu(true);
        this.getActivity().setTitle(R.string.price_title);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Price price = ((PriceAdapter)getListAdapter()).getItem(position);
        Intent intent = PricePagerActivity.newIntent(getActivity(), price.getId());
        startActivity(intent);
    }

    private class PriceAdapter extends ArrayAdapter<Price> {
        private List<Price> mPrices;
        
        public PriceAdapter(List<Price> prices) {
            super(getActivity(), 0, prices);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_price, null);
            }

            Price p = getItem(position);

            TextView titleTextView = (TextView)convertView.findViewById(R.id.price_title);
            TextView dateTextView = (TextView)convertView.findViewById(R.id.price_date);
            titleTextView.setText(p.getTitle());
            dateTextView.setText(p.getDate().toString());
            return convertView;
        }
        public void setPrices(List<Price> prices) {
             mPrices = prices;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    @Override
    public void onPause() {
        super.onPause();
        updateUI();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.list_item_context, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        ListView listView = (ListView)v.findViewById(android.R.id.list);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(listView);
        } else {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    // Required, but not used in this implementation
                }

                //ActionMode.Callback methods
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_delete:
                            PriceAdapter mAdapter = (PriceAdapter) getListAdapter();
                            PriceLab priceLab = PriceLab.get(getActivity());
                            for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
                                if (getListView().isItemChecked(i)) {
                                    priceLab.deletePrice(mAdapter.getItem(i));
                                    Intent intent = new Intent(getContext(), PriceListActivity.class);
                                    startActivity(intent);
                                }
                            }
                            mode.finish();
                            mAdapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }
                }

                public void onDestroyActionMode(ActionMode mode) {
                    //Required but not used in this implementation
                }
            });
        }
        updateUI();
        return v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        PriceAdapter mAdapter = (PriceAdapter)getListAdapter();
        Price price = mAdapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.delete_price:
                PriceLab.get(getActivity()).deletePrice(price);
                mAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_price:
                Intent intent = new Intent(getContext(), NewPriceActivity.class);
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
        PriceLab priceLab = PriceLab.get(getActivity());
        int priceCount = priceLab.getPrices().size();
        String subtitle = getString(R.string.subtitle_format, priceCount);

        if (!mSubtitleVisible) {
            subtitle = null;
        }
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    public void updateUI() {
        PriceLab priceLab = PriceLab.get(getActivity());
        List<Price> prices = priceLab.getPrices();
        if (mAdapter == null) {
            mAdapter = new PriceAdapter((List)prices);
            setListAdapter(mAdapter);
        } else {
            mAdapter.setPrices(prices);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }
}
