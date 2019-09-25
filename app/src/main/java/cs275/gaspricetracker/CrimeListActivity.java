package cs275.gaspricetracker;

import androidx.fragment.app.Fragment;

import cs275.gaspricetracker.SingleFragmentActivity;

public class CrimeListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
