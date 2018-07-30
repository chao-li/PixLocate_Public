package clidev.pixlocate.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.Activities.AddImageActivity;
import clidev.pixlocate.Activities.CamUploadActivity;
import clidev.pixlocate.Activities.DetailedImageActivity;
import clidev.pixlocate.BuildConfig;
import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.Download.FirebaseDownloadFromLocationUtilities;
import clidev.pixlocate.Keys.PutExtraKeys;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.R;
import clidev.pixlocate.RecyclerViewAdapters.GalleryRecyclerViewAdapter;
import clidev.pixlocate.Utilities.InfoUtilities;
import clidev.pixlocate.Utilities.LastKnownLocationUtilities;
import clidev.pixlocate.Utilities.MapZoomFactorUtil;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;

public class GalleryMapFragment extends Fragment
        implements LastKnownLocationUtilities.LocationListenerHandler,
        OnMapReadyCallback,
        GalleryRecyclerViewAdapter.ItemSelectionHandler,
        FirebaseDownloadFromLocationUtilities.LocationQueryHandler {

    private Location mCurrentLocation;
    private GoogleMap mMap;
    private Toast mToast;
    private FirebaseImageWithLocation mSelectedImage;

    // recyclerview and adapters
    private GalleryRecyclerViewAdapter mGalleryRecyclerViewAdapter;
    private GridLayoutManager mGridLayoutManager;

    // interface
    private GalleryFloatButtonHandler mGalleryFloatButtonHandler;

    @BindView(R.id.frag_gallery_map_mapview)
    MapView mMapView;
    @BindView(R.id.frag_gallery_map_recyclerview)
    RecyclerView mGalleryRecyclerView;
    @BindView(R.id.frag_gallery_map_progress_bar)
    ProgressBar mRecyclerViewProgress;
    @BindView(R.id.frag_gallery_map_range_text)
    TextView mRangeText;
    @BindView(R.id.gallery_adview)
    AdView mAdView;
    @BindView(R.id.frag_gallery_map_add_photo)
    FloatingActionButton mAddPhoto;
    @BindView(R.id.frag_gallery_map_camera)
    FloatingActionButton mGoCamera;
    @BindView(R.id.frag_gallery_map_refresh)
    FloatingActionButton mRefreshPage;
    @BindView(R.id.frag_gallery_map_info)
    FloatingActionButton mInfo;
    @BindView(R.id.frag_gallery_next_button) FloatingActionButton mNavigateNext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mGalleryFloatButtonHandler = (GalleryFloatButtonHandler) context;
    }

    public interface GalleryFloatButtonHandler {
        void onGalleryRefreshRequested();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if it was the location request
        if (requestCode == RequestCodes.FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Utilities.cancelToast(mToast);
                    mToast.makeText(getContext(), "App will not function properly without location access!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //mMap.setMyLocationEnabled(true);
                    getLastKnownLocation();
                }
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery_map, container, false);

        ButterKnife.bind(this, rootView);

        if (BuildConfig.DEBUG) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("PLACE HOLDER")
                    .build();

            //mAdView.loadAd(adRequest);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }


        // setting up mapview
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setFloatActionButtonListeners();

        getLastKnownLocation();

        setMapLongClickListener();
    }

    private void setMapLongClickListener() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (mRecyclerViewProgress.getVisibility() == View.INVISIBLE) {
                    // clear map of markers
                    mMap.clear();

                    // create marker
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("You searched here!")).showInfoWindow();


                    // if existing data exist in RV, clear it
                    if (mGalleryRecyclerViewAdapter != null) {
                        mGalleryRecyclerViewAdapter.clear();
                    }

                    // load image near this location
                    if (NetworkUtilities.isNetworkAvailable(getContext())) {
                        turnOnProgressBar();

                        FirebaseDownloadFromLocationUtilities firebaseDownloadFromLocationUtilities =
                                new FirebaseDownloadFromLocationUtilities(GalleryMapFragment.this);

                        firebaseDownloadFromLocationUtilities.queryThisLocationForImage(latLng, true, 12);
                    } else {
                        NetworkUtilities.alertNetworkNotAvailable(getContext());
                    }
                }
            }
        });


    }

    private void setFloatActionButtonListeners() {
        // setting up listener for floating action buttons
        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddImageActivity.class);
                startActivity(intent);
            }
        });

        mGoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CamUploadActivity.class);
                startActivity(intent);
            }
        });

        mRefreshPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // let activity know to reboot fragment
                mGalleryFloatButtonHandler.onGalleryRefreshRequested();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // show dialog box, asking if people want to exit app.
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Hint")
                        .setMessage(InfoUtilities.galleryInfo())
                        .setPositiveButton("Ok", null)
                        .show();

            }
        });

        mNavigateNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSelectedImage != null) {

                    Intent intent = new Intent(getContext(), DetailedImageActivity.class);
                    intent.putExtra(PutExtraKeys.IMAGE_OBJECT, mSelectedImage);

                    getContext().startActivity(intent);

                } else {
                    Utilities.cancelToast(mToast);
                    mToast.makeText(getContext(), "Please select a photo first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void getLastKnownLocation() {
        // Begin by getting the last known location utilities
        LastKnownLocationUtilities lastKnownLocationUtilities =
                new LastKnownLocationUtilities(getContext(), this, this);

        lastKnownLocationUtilities.setupLocationUpdate();
    }


    @Override
    public void OnLocationChanged(Location location) {
        if (location != null) {
            mCurrentLocation = location;

            //  move map to our location
            moveCameraToCurrentPosition(location);


            queryLocationForPhoto();


        }
    }


    @Override
    public void OnLocationFailed() {
        Utilities.cancelToast(mToast);
        mToast.makeText(getContext(), "Unable to get your current location. Please check that your GPS is turned on!",
                Toast.LENGTH_LONG).show();
    }

    private void moveCameraToCurrentPosition(Location location) {
        mMap.clear();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("You are here!")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapZoomFactorUtil.MAP_ZOOM));
    }


    private void queryLocationForPhoto() {
        if (NetworkUtilities.isNetworkAvailable(getContext())) {
            // convert location to a simple latlng
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            turnOnProgressBar();

            FirebaseDownloadFromLocationUtilities firebaseDownloadFromLocationUtilities =
                    new FirebaseDownloadFromLocationUtilities(this);

            firebaseDownloadFromLocationUtilities.queryThisLocationForImage(latLng, true, 12);
        } else {
            NetworkUtilities.alertNetworkNotAvailable(getContext());
        }
    }


    // Firebase location callback ////////////////////////////////////////////////////////////
    @Override
    public void onImageDataFound(FirebaseImageWithLocation firebaseImageWithLocation, Boolean searchEntireEarth) {

        turnOffProgressBar();

        // setting the recycler view

        if (mGalleryRecyclerViewAdapter != null) {

            mGalleryRecyclerViewAdapter.setNewData(firebaseImageWithLocation);

        } else {
            // otherwise create the recyclerview adapter
            mGalleryRecyclerViewAdapter = new GalleryRecyclerViewAdapter(getContext(), this, false);

            mGalleryRecyclerView.setAdapter(mGalleryRecyclerViewAdapter);

            mGridLayoutManager = new GridLayoutManager(getContext(), 3,
                    GridLayoutManager.VERTICAL, false);

            mGalleryRecyclerView.setLayoutManager(mGridLayoutManager);

            mGalleryRecyclerViewAdapter.setNewData(firebaseImageWithLocation);
        }

    }

    @Override
    public void onSearchRadiusSet(Double radius) {

        if (radius != null) {
            mRangeText.setText("Search radius = " + radius + "km");
        } else {
            mRangeText.setText("Search radius = Earth");
        }

    }

    @Override
    public void onNoImageFound() {
        turnOffProgressBar();

        Utilities.cancelToast(mToast);
        mToast.makeText(getContext(),
                "An error has occurred... try again later.",
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onImageRemoved(String key) {
        mGalleryRecyclerViewAdapter.removeDataByKey(key);
    }
    //////////////////////////////////////////////////////////////////////////////////////

    // when user click on item
    @Override
    public void OnPhotoHighlighted(Double latitude, Double longitude, FirebaseImageWithLocation selectedImage) {
        mMap.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Photo location!")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapZoomFactorUtil.MAP_ZOOM));

        // save the selected image
        mSelectedImage = selectedImage;
    }

    @Override
    public void OnPhotoLongPressed(FirebaseImageWithLocation selectedImage) {
        // do nothing
        Utilities.cancelToast(mToast);
        mToast.makeText(getContext(), "You can only delete photo inside the personal gallery",
                Toast.LENGTH_SHORT).show();
    }

    /////////////////////////


    private void turnOnProgressBar() {
        mRecyclerViewProgress.setVisibility(View.VISIBLE);
        mRangeText.setVisibility((View.VISIBLE));
    }

    private void turnOffProgressBar() {
        mRecyclerViewProgress.setVisibility(View.INVISIBLE);

        // show the search range for 2 second after search complete
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRangeText.setVisibility((View.INVISIBLE));
                mRangeText.setText("");
            }
        }, 2000);

    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMap != null) {
            mMap.setMyLocationEnabled(false);
        }
        mMapView.onDestroy();
    }


}
