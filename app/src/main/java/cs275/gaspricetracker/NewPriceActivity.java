package cs275.gaspricetracker;

import android.app.Dialog;
import android.content.DialogInterface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import androidx.fragment.app.Fragment;

public class NewPriceActivity extends SingleFragmentActivity {
    private static final int REQUEST_ERROR = 0;
    @Override
    protected Fragment createFragment() {
        return NewPriceFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int errorCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = apiAvailability
                    .getErrorDialog(this, errorCode, REQUEST_ERROR,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface idalog) {
                                    finish();
                                }
                            });

            errorDialog.show();

        }
    }

}
