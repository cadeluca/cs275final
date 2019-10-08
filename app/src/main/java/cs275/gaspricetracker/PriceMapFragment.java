package cs275.gaspricetracker;

import com.google.android.gms.maps.SupportMapFragment;

class PriceMapFragment extends SupportMapFragment {

//
//    private static final String TAG = "PriceMapFragment";
//    private static final String[] LOCATION_PERMISSIONS = new String[]{
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//    };
//    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
//    private GoogleApiClient mClient;
//    private GoogleMap mMap;
//    private Bitmap mMapImage;
//    private Location mCurrentLocation;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        activity.getSupportActionBar().setTitle("Price Map");
//
//
//        mClient = new GoogleApiClient().Builder(getActivity()).build();
//
//
//        getMapAsync(googleMap -> mMap = googleMap);
//    }
//
//    private void updateUI() {
//        if (mMap == null || mMapImage == null) {
//            return;
//        }
//        LatLng itemPoint = new LatLng(mMapItem.getLat(), mMapItem.getLon());
//        LatLng myPoint = new LatLng(
//                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        LatLngBounds bounds = new LatLngBounds.Builder()
//                .include(itemPoint)
//                .include(myPoint)
//                .build();
//        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
//        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
//        mMap.animateCamera(update);
//    }
}
