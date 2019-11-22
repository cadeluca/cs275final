package cs275.gaspricetracker;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.fragment.app.Fragment;

public class PriceListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PriceListFragment();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(PriceListActivity.this, MainActivity.class);;
            startActivity(intent);
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }

    }
}
