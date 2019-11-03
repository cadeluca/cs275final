package cs275.gaspricetracker;

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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.fragment.app.ListFragment;
import cs275.gaspricetracker.R;

public class PriceListFragment extends ListFragment {
    private ArrayList<Price> mPrices;
    private boolean mSubtitleVisible;
    private static final String TAG = "PriceListFragment";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_price_list, menu);
        MenuItem showSubtitle = menu.findItem(R.id.show_subtitle);
        showSubtitle.setTitle(R.string.hide_subtitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mSubtitleVisible = false;
        setHasOptionsMenu(true);
        this.getActivity().setTitle(R.string.price_title);
        this.mPrices = (ArrayList<Price>) PriceLab.get(getActivity()).getPrices();
//        ArrayAdapter<Price> adapter = new ArrayAdapter<Price>(getActivity(), android.R.layout.simple_list_item_1, mPrices);
        PriceAdapter adapter = new PriceAdapter(mPrices);
        setListAdapter(adapter);
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //Price c= (Price)(getListAdapter()).getItem(position);
        Price c = ((PriceAdapter)getListAdapter()).getItem(position);

        // Start PriceActivity
        //Intent i = new Intent(getActivity(), PriceActivity.class);

        // Start PricePagerActivity with this price
        Intent i = new Intent(getActivity(), PricePagerActivity.class);
        i.putExtra(PriceFragment.ARG_PRICE_ID, c.getId());
        startActivity(i);
    }
    private class PriceAdapter extends ArrayAdapter<Price> {
        public PriceAdapter(ArrayList<Price> prices) {
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
    }
    @Override
    public void onResume() {
        super.onResume();
        ((PriceAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.list_item_context, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        if (mSubtitleVisible)
            getActivity().getActionBar().setSubtitle(R.string.show_subtitle);
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
                            PriceAdapter adapter = (PriceAdapter) getListAdapter();
                            PriceLab priceLab = PriceLab.get(getActivity());
                            for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                if (getListView().isItemChecked(i)) {
                                    priceLab.deletePrice(adapter.getItem(i));
                                }
                            }
                            mode.finish();
                            adapter.notifyDataSetChanged();
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
        return v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        PriceAdapter adapter = (PriceAdapter)getListAdapter();
        Price price = adapter.getItem(position);

        switch (item.getItemId()) {
            case R.id.delete_price:
                PriceLab.get(getActivity()).deletePrice(price);
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_price:
                Price price = new Price();
                PriceLab.get(getActivity()).addPrice(price);
                Intent i = new Intent(getActivity(), PricePagerActivity.class);
                i.putExtra(PriceFragment.ARG_PRICE_ID, price.getId());
                startActivityForResult(i, 0);
                return true;

            case R.id.show_subtitle:
                if (getActivity().getActionBar().getSubtitle() == null) {
                    getActivity().getActionBar().setSubtitle(R.string.show_subtitle);
                    mSubtitleVisible = true;
                    item.setTitle(R.string.hide_subtitle);
                }
                else {
                    getActivity().getActionBar().setSubtitle(null);
                    mSubtitleVisible = false;
                    item.setTitle(R.string.show_subtitle);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
