package cs275.gaspricetracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
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
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PriceListFragment extends ListFragment {


    private boolean mSubtitleVisible;
    private static final String TAG = "PriceListFragment";
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private static final String SAVED_SORT_TITLE = "sortTitle";
    private PriceAdapter mAdapter;
    private boolean mIsSort;

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

        MenuItem sortTitleItem =  menu.findItem(R.id.price_sort);
        if (mIsSort) {
            sortTitleItem.setTitle(R.string.default_order);
        } else {
            sortTitleItem.setTitle(R.string.price_sort);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        this.getActivity().setTitle(R.string.price_header);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Price price = ((PriceAdapter)getListAdapter()).getItem(position);
        Intent intent = PricePagerActivity.newIntent(getActivity(), price.getId(), mIsSort);
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

            TextView titleTextView = convertView.findViewById(R.id.price_title);
            TextView dateTextView = convertView.findViewById(R.id.price_date);
            TextView priceTextView = convertView.findViewById(R.id.price_price);
            titleTextView.setText(p.getTitle());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd");
            String dateString = simpleDateFormat.format(p.getDate());
            dateTextView.setText(dateString);

            String priceString = String.format("$%.2f", p.getGasPrice());
            priceTextView.setText(priceString);

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
            mIsSort = savedInstanceState.getBoolean(SAVED_SORT_TITLE);
        }


        ListView listView = v.findViewById(android.R.id.list);
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
                                    Price deletePrice = mAdapter.getItem(i);
                                    priceLab.deletePrice(deletePrice);
                                    new PriceFragment.DeletePriceAsync().execute(deletePrice);

                                    // encode imageTitle for delete Image on Server
                                    byte[] byteImageTitle = deletePrice.getPhotoFilename2().getBytes();
                                    String mEncodeImageTitle = Base64.encodeToString(byteImageTitle,Base64.DEFAULT);

                                    // delete from server
                                    new DeleteImageAsync().execute(mEncodeImageTitle);
                                }
                            }
                            mode.finish();
                            updateUI();
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
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
        outState.putBoolean(SAVED_SORT_TITLE, mIsSort);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_price:
                Intent intent = new Intent(getContext(), NewPriceActivity.class);
                startActivity(intent);
                return true;
            case R.id.price_sort:
                mIsSort = !mIsSort;
                getActivity().invalidateOptionsMenu();
                updateUI();
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

    private void updateUI() {
        PriceLab priceLab = PriceLab.get(getActivity());
        List<Price> prices;
        if (mIsSort) {
            prices = priceLab.getPrices("price");
        } else {
            prices = priceLab.getPrices();
        }

        mAdapter = new PriceAdapter(prices);
        setListAdapter(mAdapter);

        updateSubtitle();
    }

    private class DeleteImageAsync extends AsyncTask<String, Void, Void> {

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
}