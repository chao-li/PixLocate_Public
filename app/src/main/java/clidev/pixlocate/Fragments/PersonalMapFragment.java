package clidev.pixlocate.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ProgressBar;
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
import clidev.pixlocate.FirebaseUtilities.Delete.FirebaseDeletePrivateImage;
import clidev.pixlocate.FirebaseUtilities.Delete.FirebaseDeletePublicImage;
import clidev.pixlocate.FirebaseUtilities.Download.FirebaseDownloadFromPersonal;
import clidev.pixlocate.Keys.PutExtraKeys;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.R;
import clidev.pixlocate.RecyclerViewAdapters.GalleryRecyclerViewAdapter;
import clidev.pixlocate.Utilities.InfoUtilities;
import clidev.pixlocate.Utilities.LastKnownLocationUtilities;
import clidev.pixlocate.Utilities.MapZoomFactorUtil;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class PersonalMapFragment extends Fragment implements OnMapReadyCallback,
    LastKnownLocationUtilities.LocationListenerHandler,
    FirebaseDownloadFromPersonal.PersonalPhotoHandler,
    GalleryRecyclerViewAdapter.ItemSelectionHandler, FirebaseDeletePublicImage.DeletePublicImageHandler, FirebaseDeletePrivateImage.DeletePrivateImageHandler {

    private GoogleMap mMap;
    private Toast mToast;

    // Recyclerview
    private GalleryRecyclerViewAdapter mRecyclerViewAdapter;
    private GridLayoutManager mGridLayoutManager;
    private Boolean isSwipeDeleteAllowed;
    private FirebaseImageWithLocation mSelectedImage;

    // interface variable
    private PersonalFloatButtonHandler mPersonalFloatButtonHandler;


    // for implementing onscroll listener
    private int mPreviousTotalItemCount;
    private Boolean isLoading;


    @BindView(R.id.frag_personal_map_mapview) MapView mMapView;
    @BindView(R.id.frag_personal_map_recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.frag_personal_map_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.personal_adview) AdView mAdView;
    @BindView(R.id.frag_personal_map_add_photo) FloatingActionButton mAddPhoto;
    @BindView(R.id.frag_personal_map_camera) FloatingActionButton mGoCamera;
    @BindView(R.id.frag_personal_map_refresh) FloatingActionButton mRefreshPage;
    @BindView(R.id.frag_personal_map_info) FloatingActionButton mInfo;
    @BindView(R.id.frag_personal_next_button) FloatingActionButton mNavigateNext;




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPersonalFloatButtonHandler = (PersonalFloatButtonHandler) context;
    }

    public interface PersonalFloatButtonHandler {
        void onPersonalRefreshRequeted();
    }


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
                    //mMap.setMyLocationEnabled(true);
                    getLastKnownLocation();
                }
            }

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_personal_map, container, false);

        ButterKnife.bind(this, rootView);

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

        // allowing swipe delete?
        isSwipeDeleteAllowed = true;

        // is loading at the start?
        isLoading = false;

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


    // run these codes when map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        setFloatActionButtonListeners();

        getLastKnownLocation();

        // start loading photos

        queryPersonalPhoto();
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
                // tell activity to refresh fragment
                mPersonalFloatButtonHandler.onPersonalRefreshRequeted();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // show dialog box, asking if people want to exit app.
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Hint")
                        .setMessage(InfoUtilities.personalInfo())
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


    // Moving camera to last known location
    private void getLastKnownLocation() {
        // Begin by getting the last known location utilities
        LastKnownLocationUtilities lastKnownLocationUtilities =
                new LastKnownLocationUtilities(getContext(), this, this);

        lastKnownLocationUtilities.setupLocationUpdate();
    }

    @Override
    public void OnLocationChanged(Location location) {
        // take user to current location.
        moveCameraToCurrentPosition(location);
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

    //////////////////////////////////////////////////////////////////////////



    // Firebase query call back //////////////////////////////////////////////////////////////
    private void queryPersonalPhoto() {
        if (NetworkUtilities.isNetworkAvailable(getContext())) {
            turnOnProgressBar();
            FirebaseDownloadFromPersonal firebaseDownloadFromPersonal = new FirebaseDownloadFromPersonal(this);
            firebaseDownloadFromPersonal.queryPersonalDatabase();
        } else {
            NetworkUtilities.alertNetworkNotAvailable(getContext());
        }
    }


    @Override
    public void onPersonalPhotoFound(FirebaseImageWithLocation firebaseImageWithLocation) {
        turnOffProgressBar();

        if (mRecyclerViewAdapter != null) {

            mRecyclerViewAdapter.setNewData(firebaseImageWithLocation);

        } else {
            // otherwise create the recyclerview adapter
            mRecyclerViewAdapter = new GalleryRecyclerViewAdapter(getContext(), this, true);

            mRecyclerView.setAdapter(mRecyclerViewAdapter);

            mGridLayoutManager = new GridLayoutManager(getContext(), 3,
                    GridLayoutManager.VERTICAL, false);

            mRecyclerView.setLayoutManager(mGridLayoutManager);

            mRecyclerViewAdapter.setNewData(firebaseImageWithLocation);

        }


        //attachOnSwipeDelete(); // no longer using swipe to delete

        attachOnScrollListener();

    }

    @Override
    public void onPersonalPhotoNotFound() {
        turnOffProgressBar();
    }

    @Override
    public void onScrollPhotoFound(FirebaseImageWithLocation firebaseImageWithLocation) {
        turnOffProgressBar();

        mRecyclerViewAdapter.setNewData(firebaseImageWithLocation);

    }
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void OnPhotoHighlighted(Double latitude, Double longitude, FirebaseImageWithLocation selectedImage) {
        mMap.clear();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Photo location!")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MapZoomFactorUtil.MAP_ZOOM));

        mSelectedImage = selectedImage;
    }

    @Override
    public void OnPhotoLongPressed(final FirebaseImageWithLocation selectedImage) {

        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            new AlertDialog.Builder(getContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Are you sure??")
                    .setMessage("Delete is permanent, are you sure you want to delete this photo?")
                    .setPositiveButton("Yes, I'm sure!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deletePhoto(selectedImage);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    })
                    .show();
        }


    }


    // on swipe delete ////////////////////////////////////////////////

    /*
    private void attachOnSwipeDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean isItemViewSwipeEnabled() {

                if (isSwipeDeleteAllowed == true) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                // create alert dialog to confirm delete process
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure??")
                        .setMessage("Delete is permanent, are you sure you want to delete this photo?")
                        .setPositiveButton("Yes, I'm sure!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deletePhoto(viewHolder);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mRecyclerViewAdapter.notifyDataSetChanged();
                            }
                        })
                        .show();


            }
        }).attachToRecyclerView(mRecyclerView);


    }
    */

    private void deletePhoto(FirebaseImageWithLocation selectedImage) {
        if (NetworkUtilities.isNetworkAvailable(getContext())) {
            isSwipeDeleteAllowed = false;
            turnOnProgressBar();

            // get the data corresponding to the item the user swiped.
            FirebaseImageWithLocation firebaseImageWithLocation = selectedImage;

            if (firebaseImageWithLocation.getPrivatePhoto() == false) {

                FirebaseDeletePublicImage firebaseDeletePublicImage =
                        new FirebaseDeletePublicImage(PersonalMapFragment.this);
                firebaseDeletePublicImage.deleteImage(firebaseImageWithLocation);

            } else {

                // delete private photo
                FirebaseDeletePrivateImage firebaseDeletePrivateImage =
                        new FirebaseDeletePrivateImage(PersonalMapFragment.this);
                firebaseDeletePrivateImage.deleteImage(firebaseImageWithLocation);


            }

            // delete object from recycler view.
            //mRecyclerViewAdapter.deleteItem(firebaseImageWithLocation);
        } else {
            NetworkUtilities.alertNetworkNotAvailable(getContext());
            //mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }


    // on delete call back
    @Override
    public void onPublicImageDeleteSuccess(FirebaseImageWithLocation deletedData) {
        deleteSuccessMessage(deletedData);
    }

    @Override
    public void onPublicImageDeleteFail(FirebaseImageWithLocation firebaseImageWithLocation, String failReason) {
        deleteFailedMessage(firebaseImageWithLocation);
    }

    @Override
    public void onPrivateImageDeleteSuccess(FirebaseImageWithLocation deletedData) {
        deleteSuccessMessage(deletedData);
    }

    @Override
    public void onPrivateImageDeleteFail(FirebaseImageWithLocation firebaseImageWithLocation, String failReason) {
        deleteFailedMessage(firebaseImageWithLocation);

    }


    //////////////////////////////////////////////////////////////////////////////////////////////


    // OnScrollListener ///////////////////////////////////////////////////////////////////////
    private void attachOnScrollListener() {
        if (mRecyclerView != null) {

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    // total item in the list
                    int totalItemCount = mGridLayoutManager.getItemCount();
                    Timber.d("total item count: " + totalItemCount + "");
                    // last visible item in the list
                    int lastVisiblePosition = mGridLayoutManager.findLastVisibleItemPosition();
                    Timber.d("current scroll position: " + lastVisiblePosition + "");

                    // initiate previousTotalItemCount
                    if (mPreviousTotalItemCount == 0) {
                        mPreviousTotalItemCount = totalItemCount;
                    }

                    // if not loading, and we are at the bottom of the list, initiate loading.
                    if (isLoading == false && lastVisiblePosition >= totalItemCount - 4) {
                        // first find the key of the first element in the RecyclerView adapter.
                        FirebaseImageWithLocation lastItem = mRecyclerViewAdapter.getLastItem();
                        String key = lastItem.getImageKey();

                        turnOnProgressBar();

                        // load the next batch of data from firebase
                        FirebaseDownloadFromPersonal firebaseDownloadFromPersonal =
                                new FirebaseDownloadFromPersonal(PersonalMapFragment.this);
                        firebaseDownloadFromPersonal.scrollQueryPersonalDatabase(key);


                        isLoading = true;
                    }


                    // if we are currently loading, check if loading has completed
                    if (isLoading == true && totalItemCount >=
                            mPreviousTotalItemCount + FirebaseDownloadFromPersonal.NUM_SCROLL_LOAD - 1) {
                        isLoading = false;
                        mPreviousTotalItemCount = totalItemCount;
                    }
                }
            });
        }
    }

    //...........................................................................









    private void deleteSuccessMessage(FirebaseImageWithLocation deletedData) {
        isSwipeDeleteAllowed = true;
        turnOffProgressBar();

        // delete the image from recylerview
        mRecyclerViewAdapter.deleteItem(deletedData);

        Utilities.cancelToast(mToast);
        mToast.makeText(getContext(), "Delete success", Toast.LENGTH_SHORT).show();
    }

    private void deleteFailedMessage(FirebaseImageWithLocation firebaseImageWithLocation) {
        isSwipeDeleteAllowed = true;
        turnOffProgressBar();

        //mRecyclerViewAdapter.setNewData(firebaseImageWithLocation);

        String deleteErrorText = "Failed to completely delete all reference to the image. Please try again when network is more stable.";

        new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Error")
                .setMessage(deleteErrorText)
                .setPositiveButton("Ok", null)
                .show();
    }






    private void turnOnProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void turnOffProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
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
