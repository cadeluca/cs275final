package cs275.gaspricetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;
import java.util.UUID;

public class PricePagerActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID = "cs275.gaspricetracker.price_id";
    private ViewPager mViewPager;
    private List<Price> mPrices;

    public static Intent newIntent(Context packageContext, UUID priceId) {
        Intent intent = new Intent(packageContext, PricePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, priceId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_pager);

        UUID priceId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CRIME_ID);

        mViewPager = (ViewPager) findViewById(R.id.price_view_pager);

        mPrices = PriceLab.get(this).getPrices();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            @Override
            public Fragment getItem(int position) {
                Price price = mPrices.get(position);
                return PriceFragment.newInstance(price.getId());
            }

            @Override
            public int getCount() {
                return mPrices.size();
            }
        });

        for (int i = 0; i < mPrices.size(); i++) {
            if (mPrices.get(i).getId().equals(priceId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}