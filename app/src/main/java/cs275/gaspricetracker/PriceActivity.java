package cs275.gaspricetracker;

import androidx.fragment.app.Fragment;

public class PriceActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PriceFragment();
    }

}
