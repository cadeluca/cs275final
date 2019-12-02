package cs275.gaspricetracker;

import androidx.fragment.app.Fragment;

public class PriceListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PriceListFragment();
    }
}
