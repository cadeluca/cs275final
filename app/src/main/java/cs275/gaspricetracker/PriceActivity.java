package cs275.gaspricetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import cs275.gaspricetracker.PriceFragment;
import cs275.gaspricetracker.SingleFragmentActivity;

public class PriceActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PriceFragment();
    }

}
