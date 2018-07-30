package clidev.pixlocate.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.Activities.AddImageActivity;
import clidev.pixlocate.Activities.CamUploadActivity;
import clidev.pixlocate.Activities.DetailedImageActivity;
import clidev.pixlocate.BuildConfig;
import clidev.pixlocate.MapSearchFunctions.Data.ExploreMarkers;
import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.Download.FirebaseDownloadFromLocationUtilities;
import clidev.pixlocate.GoogleMapUtils.MyItem;
import clidev.pixlocate.Keys.PutExtraKeys;
import clidev.pixlocate.MapSearchFunctions.CurrentLocationAction;
import clidev.pixlocate.MapSearchFunctions.OnMapLongClickedListeners;
import clidev.pixlocate.MapSearchFunctions.SearchLocationAction;
import clidev.pixlocate.R;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.RecyclerViewAdapters.PreviewRecyclerViewAdapter;
import clidev.pixlocate.Utilities.InfoUtilities;
import clidev.pixlocate.Utilities.MapZoomFactorUtil;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class ExploreFragment extends Fragment implements OnMapReadyCallback,
                                        GoogleMap.OnMapClickListener,
                                        SearchLocationAction.LocationSearchHandler,
                                        CurrentLocationAction.CurrentLocationHandler,
                                        OnMapLongClickedListeners.OnMapLongClickHandler,
                                        FirebaseDownloadFromLocationUtilities.LocationQueryHandler,
                                        ClusterManager.OnClusterItemClickListener,
                                        ClusterManager.OnClusterClickListener{

    // bind view using butter knife
    @BindView(R.id.map_search_edit_text) EditText mMapSearch;
    @BindView(R.id.confirm_location_button) Button mConfirmLocationButton;
    @BindView(R.id.explore_map_view) MapView mMapView;
    @BindView(R.id.explore_image_preview) ImageView mPreviewImage;
    @BindView(R.id.explore_full_image_button) Button mFullImageButton;
    @BindView(R.id.explore_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.explore_range_text) TextView mRangeText;
    @BindView(R.id.explore_adview) AdView mAdView;
    @BindView(R.id.explore_add_photo) FloatingActionButton mAddPhoto;
    @BindView(R.id.explore_camera) FloatingActionButton mGoCamera;
    @BindView(R.id.explore_info) FloatingActionButton mInfo;
    @BindView(R.id.explore_recycler_preview) RecyclerView mRecycler_Preview;
    @BindView(R.id.explore_my_photo_switch) Switch mMyPhotoSwitch;



    // Advanced Map Search Functions
    private SearchLocationAction mSearchLocationAction;
    private CurrentLocationAction mCurrentLocationAction;
    private OnMapLongClickedListeners mOnMapLongClickedListeners;

    // Google MapUtils
    private ClusterManager<MyItem> mClusterManager;

    // views
    private View mRootView;


    // fields
    private GoogleMap mMap;
    private Toast mToast;
    private Double mSearchRadius;

    // download utilities
    private FirebaseDownloadFromLocationUtilities mFirebaseDownloadFromLocationUtilities;

    // Markers
    private ExploreMarkers mExploreMarkers;
    private Boolean hasMapAlertPopped;
    private Boolean hasMapToastPopped;



    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if it was the location request
        if (requestCode == RequestCodes.FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    mToast.makeText(getContext(), "App will not function properly without location access!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentLocationAction.enableMoveToUserLocationButton(mMap);
                }
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        mRootView = rootView;

        // finding all views
        ButterKnife.bind(this, mRootView);

        // implementing ad.
        if (BuildConfig.DEBUG) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("PLACE HOLDER")
                    .build();

            //mAdView.loadAd(adRequest);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        // preset view properties
        mConfirmLocationButton.setEnabled(false);
        mPreviewImage.setVisibility(View.INVISIBLE);
        mFullImageButton.setVisibility(View.INVISIBLE);

        // Creating a new instance of data
        mExploreMarkers = new ExploreMarkers();

        // Start MapView initiation
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        // Adding Advanced map functions
        mSearchLocationAction = new SearchLocationAction(getContext(), this);
        mCurrentLocationAction = new CurrentLocationAction(getContext(), this, this);
        mOnMapLongClickedListeners = new OnMapLongClickedListeners(getContext(), this);


        return rootView;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        setFloatActionButtonListeners();

        setLocationSearchLogic();


        mMap.setOnMapClickListener(this);

        setClusterManagerListeners();


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


        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // show dialog box, asking if people want to exit app.
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Hint")
                        .setMessage(InfoUtilities.exploreInfo())
                        .setPositiveButton("Ok", null)
                        .show();




            }
        });

    }

    private void setLocationSearchLogic() {
        // logic for the search function.
        mSearchLocationAction.setSearchListener(mMapSearch);

        // logic for current location button.
        mCurrentLocationAction.enableMoveToUserLocationButton(mMap);

        // logic for when map hears long click
        mOnMapLongClickedListeners.setMapLongClickListener(mMap);
    }

    private void setClusterManagerListeners() {
        // Setting up google util's cluster manager
        mClusterManager = new ClusterManager<MyItem>(getContext(), mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    // methods that will be used by all 3 search functions that are to follow////////////////////////////
    // method for taking map to default settings
    private void clearMapToDefaultSetting() {
        // clearing cluster managers
        mClusterManager.clearItems();

        hasMapAlertPopped = false; // probably don't need anymore. previously used to show an alert when search range is too big
        hasMapToastPopped = false;

        mMap.clear();

        ////////////////////////////////

        // clear all markers.
        mExploreMarkers.clearPhotoLatLng();
        mExploreMarkers.clearRequestMarker();

        // clear the preview window
        clearPhotoPreview();

        hideProgressBar();
    }

    // method for starting photo search
    private void findPhotoInThisLocation(LatLng latLng) {
        if (NetworkUtilities.isNetworkAvailable(getContext())) {
            showProgressBarWithRange();

            if (mMyPhotoSwitch.isChecked()) {
                Timber.d("querying user's own photo");
                mFirebaseDownloadFromLocationUtilities = new FirebaseDownloadFromLocationUtilities(ExploreFragment.this);
                mFirebaseDownloadFromLocationUtilities.queryThisLocationForImage(latLng, false, 12);
            } else {
                Timber.d("querying public photo");
                mFirebaseDownloadFromLocationUtilities = new FirebaseDownloadFromLocationUtilities(ExploreFragment.this);
                mFirebaseDownloadFromLocationUtilities.queryThisLocationForImage(latLng, true, 12);
            }


        } else {
            NetworkUtilities.alertNetworkNotAvailable(getContext());
            mConfirmLocationButton.setEnabled(true);
        }
    }

    // method for creating and saving the requested location marker
    private void createAndSaveRequestMarkerNoZoom(LatLng latLng) {
        mExploreMarkers.setRequestMarker(mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Requested location!")));

        mExploreMarkers.getRequestMarker().showInfoWindow();

        mExploreMarkers.setRequestMarkerId(
                mExploreMarkers.getRequestMarker().getId()
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////




    // Current Location button feedback /////////////////////////////////////////////
    @Override
    public void onCurrentLocationResult(Location location) {
        clearMapToDefaultSetting();

        final LatLng latLng = zoomToLocation(location);

        activateConfirmButton(latLng);
    }

    private void activateConfirmButton(final LatLng latLng) {
        mConfirmLocationButton.setEnabled(true);

        mConfirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear(); // clear requested marker from the map

                mConfirmLocationButton.setEnabled(false);

                // once confirm location is clicked... begin searching for photo in this region.
                findPhotoInThisLocation(latLng);
            }
        });
    }



    @NonNull
    private LatLng zoomToLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        createAndSaveRequestMarkerNoZoom(latLng);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapZoomFactorUtil.MAP_ZOOM));
        return latLng;
    }



    ////////////////////////////////////////////////////////////////////


    // Map Search result feedbacks /////////////////////////////////////////////////////////////////////
    @Override
    public void onSearchFieldEntered() {

        clearMapToDefaultSetting();

        showProgressBar();
    }

    @Override
    public void onSearchResultObtained(Address address) {
        hideProgressBar();

        // create marker for this location, and move camera to this location.
        zoomToSearchedLocation(address);

        // display confirm location
        activateConfirmLocationButton(address);
    }

    @Override
    public void onSearchResultFailed(Boolean isFailed) {
        hideProgressBar();
        if (isFailed == true) {
            // Let user know couldn't not find location
            Utilities.cancelToast(mToast);
            mToast.makeText(getContext(),
                    "Cannot find this location. Please be more specific with your address.", Toast.LENGTH_LONG)
            .show();
        }
    }

    private void zoomToSearchedLocation(Address address) {
        LatLng searchedLocation = new LatLng(address.getLatitude(), address.getLongitude());

        createAndSaveRequestMarkerNoZoom(searchedLocation);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, MapZoomFactorUtil.MAP_ZOOM));
    }


    private void activateConfirmLocationButton(final Address address) {
        mConfirmLocationButton.setEnabled(true);

        mConfirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear(); // clear requested marker from the map

                mConfirmLocationButton.setEnabled(false);

                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                findPhotoInThisLocation(latLng);
            }
        });
    }


    /////////////////////////////////////////////////////////////////////////////////////////



    // WHEN MAP HEARS LONG CLICK ////////////////////////////////////////////////

    @Override
    public void onMapLongClicked(LatLng latLng) {
        Timber.d("Map long click received");

        clearMapToDefaultSetting();

        // create a marker at where long click was clicked.
        createAndSaveRequestMarkerNoZoom(latLng);

        // set confirmation button to allow searching for location
        activateConfirmButtonOnMapLongClick(latLng);
    }




    private void activateConfirmButtonOnMapLongClick(final LatLng latLng) {
        mConfirmLocationButton.setEnabled(true);

        mConfirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear(); // clear requested marker from the map

                mConfirmLocationButton.setEnabled(false);

                findPhotoInThisLocation(latLng);
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////












    // Firebase on photo downloaded ///////////////////////////////////
    @Override
    public void onImageDataFound(FirebaseImageWithLocation firebaseImageWithLocation, Boolean searchedEntireEarth) {
        hideProgressBar();

        Timber.d(firebaseImageWithLocation.getImageKey());

        //  create the marker based on location and the username
        LatLng latLng = new LatLng(firebaseImageWithLocation.getLatitude(),
                firebaseImageWithLocation.getLongitude());


        // Save the marker into the clusterManager
        mExploreMarkers.addPhotoLatLng(latLng);

        mClusterManager.addItem(new MyItem(latLng.latitude, latLng.longitude, firebaseImageWithLocation));



        if (searchedEntireEarth == false) {
            // Create bounds to capture all current markers, which includes requested markers
            // create bound builder
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng photoLatLng : mExploreMarkers.getPhotoLatLngList()) {
                builder.include(photoLatLng);
            }

            // create bounds

            LatLngBounds bounds = builder.build();
            int padding = 100; // offset from the edges of the map in pixels
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            // now move the camera
            mMap.animateCamera(cameraUpdate);

            if (hasMapToastPopped == false) {
                Utilities.cancelToast(mToast);
                mToast.makeText(getContext(), "Please move/zoom map a bit to help markers render!", Toast.LENGTH_LONG).show();
                hasMapToastPopped = true;
            }
        } else {

            if (hasMapToastPopped == false) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3));

                Utilities.cancelToast(mToast);
                mToast.makeText(getContext(), "Please move/zoom map a bit. Search radius too big, unable to capture all markers.", Toast.LENGTH_LONG).show();
                hasMapToastPopped = true;
            }

        }


    }


    @Override
    public void onSearchRadiusSet(Double radius) {
        if (radius != null) {
            mSearchRadius = radius;
            mRangeText.setText("Search radius = " + radius + "km");
        } else {
            mSearchRadius = 10000d;
            mRangeText.setText("Search radius = Earth");
        }
    }

    @Override
    public void onNoImageFound() {
        hideProgressBar();
        // no need for this method
        Utilities.cancelToast(mToast);
        mToast.makeText(getContext(), "No results found", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onImageRemoved(String key) {
        // need to write a method to delete the markers that were deleted by other users.

    }
    ///////////////////////////////////////////////////////////////////////////////


    // When any of the cluster or cluster items are clicked ////////////////////////////////////////
    @Override
    public boolean onClusterClick(final Cluster cluster) {
        clearPhotoPreview();

        ArrayList<FirebaseImageWithLocation> imageDatas = getImageDataWithinThisCluster(cluster);

        setImageDataToPreviewRecyclerView(imageDatas);

        return true;
    }


    @NonNull
    private ArrayList<FirebaseImageWithLocation> getImageDataWithinThisCluster(Cluster cluster) {
        Timber.d("cluster clicked");


        List<ClusterItem> clusterItems =
                (List<ClusterItem>) cluster.getItems();

        ArrayList<FirebaseImageWithLocation> imageDatas = new ArrayList<>();

        for (ClusterItem clusterItem : clusterItems) {
            imageDatas.add(((MyItem) clusterItem).getFirebaseImageWithLocation());
        }
        return imageDatas;
    }

    private void setImageDataToPreviewRecyclerView(ArrayList<FirebaseImageWithLocation> imageDatas) {
        PreviewRecyclerViewAdapter previewRecyclerViewAdapter =
                new PreviewRecyclerViewAdapter(getContext());

        mRecycler_Preview.setAdapter(previewRecyclerViewAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2,
                GridLayoutManager.VERTICAL, false);

        mRecycler_Preview.setLayoutManager(gridLayoutManager);

        previewRecyclerViewAdapter.setAllData(imageDatas);

        mRecycler_Preview.setVisibility(View.VISIBLE);
    }





    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        clearPhotoPreview();

        Timber.d("cluster item clicked");
        final FirebaseImageWithLocation previewImageData =
                ((MyItem) clusterItem).getFirebaseImageWithLocation();


        // set image
        String imageUrl = previewImageData.getSmallImageUrl();
        RequestOptions options = new RequestOptions();
        options.centerCrop();
        options.placeholder(R.drawable.image_loading);
        options.override(200, 200);

        Glide.with(getContext())
                .load(imageUrl)
                .apply(options)
                .into(mPreviewImage);

        // make preview and button visible
        mPreviewImage.setVisibility(View.VISIBLE);
        mFullImageButton.setVisibility(View.VISIBLE);


        mFullImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getContext(), DetailedImageActivity.class);
                intent.putExtra(PutExtraKeys.IMAGE_OBJECT, previewImageData);

                getContext().startActivity(intent);
            }
        });

        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////



    @Override
    public void onMapClick(LatLng latLng) {
        Timber.d("On map clicked");

        clearPhotoPreview();

    }

    private void clearPhotoPreview() {
        mFullImageButton.setOnClickListener(null);

        if(mPreviewImage.getVisibility() == View.VISIBLE) {
            mPreviewImage.setVisibility(View.INVISIBLE);
        }

        if (mFullImageButton.getVisibility() == View.VISIBLE) {
            mFullImageButton.setVisibility(View.INVISIBLE);
        }

        // clear image preview
        Glide.with(getContext()).clear(mPreviewImage);
        mPreviewImage.setImageDrawable(null);

        // clear the recycler view preview
        mRecycler_Preview.setAdapter(null);
        mRecycler_Preview.setVisibility(View.INVISIBLE);
    }

    /////////////////////////////////////////


    private void showProgressBarWithRange() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRangeText.setVisibility((View.VISIBLE));
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRangeText.setVisibility(View.INVISIBLE);
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