package cs275.gaspricetracker;

import androidx.fragment.app.Fragment;

public class PriceMapActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PriceMapFragment();
    }

}
