package cs275.gaspricetracker;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.fragment.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}


