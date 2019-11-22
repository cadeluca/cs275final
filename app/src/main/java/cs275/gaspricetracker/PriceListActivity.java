package cs275.gaspricetracker;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.fragment.app.Fragment;

public class PriceListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PriceListFragment();
    }
}
