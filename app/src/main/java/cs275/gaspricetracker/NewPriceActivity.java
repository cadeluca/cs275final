package cs275.gaspricetracker;

import androidx.fragment.app.Fragment;

public class NewPriceActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new NewPriceFragment();
    }

}
