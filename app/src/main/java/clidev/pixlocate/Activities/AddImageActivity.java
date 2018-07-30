package clidev.pixlocate.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.BuildConfig;
import clidev.pixlocate.FirebaseUtilities.Upload.FirebaseUploadPrivateFast;
import clidev.pixlocate.FirebaseUtilities.Upload.FirebaseUploadPublicFast;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.MapSearchFunctions.CurrentLocationAction;
import clidev.pixlocate.MapSearchFunctions.Data.ImageData;
import clidev.pixlocate.MapSearchFunctions.MarkerAndCameraControl;
import clidev.pixlocate.MapSearchFunctions.OnMapLongClickedListeners;
import clidev.pixlocate.MapSearchFunctions.SearchLocationAction;
import clidev.pixlocate.R;
import clidev.pixlocate.Utilities.CompressBitmapUtil;
import clidev.pixlocate.Utilities.InfoUtilities;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class AddImageActivity extends AppCompatActivity implements SearchLocationAction.LocationSearchHandler, CurrentLocationAction.CurrentLocationHandler, OnMapLongClickedListeners.OnMapLongClickHandler, OnMapReadyCallback, FirebaseUploadPrivateFast.FirebasePrivateUploadHandler, FirebaseUploadPublicFast.FirebasePublicUploadHandler {

    private static final String BITMAP = "bitmap";
    // Fields
    private GoogleMap mMap;
    private Toast mToast;
    private ImageData mImageData;


    // map search functions
    private SearchLocationAction mSearchLocationAction;
    private CurrentLocationAction mCurrentLocationAction;
    private OnMapLongClickedListeners mOnMapLongClickedListeners;


    // views
    @BindView(R.id.add_image_search_bar)
    EditText mSearchBarEditText;
    @BindView(R.id.add_image_map_view)
    MapView mMapView;
    @BindView(R.id.add_image_confirm_location)
    Button mConfirmButton;
    @BindView(R.id.add_image_confirmPhotoLayout)
    ConstraintLayout mConfirmPhotoLayout;
    @BindView(R.id.add_image_confirmImageView)
    ImageView mConfirmImageView;
    @BindView(R.id.add_image_mapLayout)
    LinearLayout mMapLayout;
    @BindView(R.id.add_image_uploadImageView)
    ImageView mUpload;
    @BindView(R.id.add_image_cancelImageView)
    ImageView mCancel;
    @BindView(R.id.add_image_uploadProgress)
    ProgressBar mProgressBar;
    @BindView(R.id.add_image_uploadProgressTextView)
    TextView mProgressText;
    @BindView(R.id.add_image_privacy_switch)
    Switch mPrivacySwitch;
    @BindView(R.id.add_image_private_text)
    TextView mPrivateText;
    @BindView(R.id.add_image_public_text)
    TextView mPublicText;
    @BindView(R.id.add_image_adview)
    AdView mAdView;
    @BindView(R.id.add_image_info)
    FloatingActionButton mInfo;
    @BindView(R.id.add_image_rotate_left) ImageView mRotateLeft;
    @BindView(R.id.add_image_rotate_right) ImageView mRotateRight;


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestCodes.FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    mToast.makeText(this, "App will not function properly without location access!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // enable the move to location button.
                    mCurrentLocationAction.enableMoveToUserLocationButton(mMap);
                }
            }

        } else if (requestCode == RequestCodes.READ_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // grab image from storage
                pickImageFromStorage();
            } else {
                mMap.clear();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        ButterKnife.bind(this);

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

        // begin map initiation
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // makes map display immediately;

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        // instanciate map search functions
        mSearchLocationAction = new SearchLocationAction(this, this);
        mCurrentLocationAction = new CurrentLocationAction(this, this);
        mOnMapLongClickedListeners = new OnMapLongClickedListeners(this, this);

        // instanciate data
        mImageData = new ImageData();

        if (savedInstanceState == null) {
            beginLoadImagePhase();
        } else {
            Timber.d("activity re-created");
            Bitmap bitmap = savedInstanceState.getParcelable(BITMAP);
            mImageData.setBitmap(bitmap);
            Glide.with(this).load(bitmap).into(mConfirmImageView);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        if (mImageData.getBitmap() != null) {
            outState.putParcelable(BITMAP, mImageData.getBitmap());
        }

    }

    // ON MAP READY //////////////////////////////////
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        setInfoFloatingActionButton();

        // initate all location search functions
        mSearchLocationAction.setSearchListener(mSearchBarEditText);
        mOnMapLongClickedListeners.setMapLongClickListener(mMap);
        mCurrentLocationAction.enableMoveToUserLocationButton(mMap);



    }

    private void setInfoFloatingActionButton() {

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // show dialog box, asking if people want to exit app.
                new AlertDialog.Builder(AddImageActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Hint")
                        .setMessage(InfoUtilities.addPhotoInfo())
                        .setPositiveButton("Ok", null)
                        .show();

            }
        });

    }

    private void beginLoadImagePhase() {
        // clear image preview
        mConfirmImageView.setImageResource(android.R.color.transparent);

        mConfirmPhotoLayout.setVisibility(View.VISIBLE);
        mMapLayout.setVisibility(View.INVISIBLE);


        // make request to get photo from storage
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // if we don't have permission to access storage
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // request permission to access storage
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCodes.READ_EXTERNAL_REQUEST_CODE);
            } else {
                // grab image from storage
                pickImageFromStorage();
            }
        } else {
            // grab image from storage
            pickImageFromStorage();
        }
    }

    private void pickImageFromStorage() {
        /*
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RequestCodes.READ_EXTERNAL_INTENT_CODE);
        */

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        /*
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        */

        startActivityForResult(getIntent, RequestCodes.READ_EXTERNAL_INTENT_CODE);

    }

    //.....................................................


    // When image returns from image selector /////////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            Uri selectedImage = data.getData();

            if (requestCode == RequestCodes.READ_EXTERNAL_INTENT_CODE && resultCode == RESULT_OK && data != null) {
                try {
                    Bitmap bitmap = CompressBitmapUtil.getResizedBitmap(
                            MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage), 500);

                    Timber.d("Image Chosen");

                    // save image data into the temporary slot
                    mImageData.setBitmap(bitmap);

                    // display this selected image
                   // mConfirmImageView.setImageBitmap(bitmap);
                    Glide.with(this).load(bitmap).into(mConfirmImageView);

                    // activate upload button function
                    upLoadButtonAction();

                    // activate cancelButtonAction
                    cancelButtonAction();

                    rotateLeftAction();

                    rotateRightAction();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // if no image selected, go back to previous activity
            onBackPressed();
        }

    }

    private void upLoadButtonAction() {
        // set upload and cancel button functionality
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                beginSetLocationPhase();


            }
        });
    }

    private void cancelButtonAction() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("cancel pressed");

                beginLoadImagePhase();
            }
        });
    }

    private void rotateLeftAction() {
        mRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("clicked rotate left");

                Bitmap bitmap = rotateBitmap(mImageData.getBitmap(), -90);

                Glide.with(AddImageActivity.this).load(bitmap).into(mConfirmImageView);

                mImageData.setBitmap(bitmap);


            }
        });

    }

    private void rotateRightAction() {
        mRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.d("clicked rotate right");

                Bitmap bitmap = rotateBitmap(mImageData.getBitmap(), 90);

                Glide.with(AddImageActivity.this).load(bitmap).into(mConfirmImageView);

                mImageData.setBitmap(bitmap);

            }
        });

    }

    private void beginSetLocationPhase() {
        mConfirmPhotoLayout.setVisibility(View.INVISIBLE);
        mMapLayout.setVisibility(View.VISIBLE);
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    // .........................................................................


    // SEARCH CALL BACK //////////////////////////////////////////
    @Override
    public void onSearchFieldEntered() {
        // clear all existing markers
        mMap.clear();

        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSearchResultObtained(Address address) {
        mProgressBar.setVisibility(View.INVISIBLE);

        // move to said location on the map.
        LatLng markerLocation = MarkerAndCameraControl.createMarker(address, null, null, mMap);
        MarkerAndCameraControl.animateToMarkerPosition(markerLocation, mMap);

        // save image location data
        mImageData.setLatLng(markerLocation);

        // activate Confirm location button
        enableConfirmButton();

    }

    @Override
    public void onSearchResultFailed(Boolean isFailed) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (isFailed == true) {
            // let user know, couldn't find location
            Utilities.cancelToast(mToast);
            mToast.makeText(this,
                    "Cannot find this location. Please be more specific with your address.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    ////////////////////////////////////////////////////////////////////


    // CURRENT LOCATION CALL BACK ////////////////////////////////////////
    @Override
    public void onCurrentLocationResult(Location location) {
        // clear map if move to location is working
        mMap.clear();

        // move to this location on the map
        LatLng markerLocation = MarkerAndCameraControl.createMarker(null, location, null, mMap);
        MarkerAndCameraControl.animateToMarkerPosition(markerLocation, mMap);

        // save image location data
        mImageData.setLatLng(markerLocation);

        // activate confirm location button
        enableConfirmButton();


    }


    ///////////////////////////////////////////////////////////////////


    // MAP LONG CLICK CALL BACK ///////////////////////////////////////////////
    @Override
    public void onMapLongClicked(LatLng latLng) {
        // clear map of other markers
        mMap.clear();

        // create a marker on this location. do not zoom to location
        LatLng markerLocation = MarkerAndCameraControl.createMarker(null, null, latLng, mMap);

        // save image location data
        mImageData.setLatLng(markerLocation);

        // activate confirm location button.
        enableConfirmButton();
    }


    //////////////////////////////////////////////////////////////////////////


    // Confirm upload button
    private void enableConfirmButton() {
        mConfirmButton.setEnabled(true);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtilities.isNetworkAvailable(AddImageActivity.this)) {
                    // show loading icon
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressText.setVisibility(View.VISIBLE);

                    // Checking state of privacy switch
                    Timber.d("Private photo: " + mPrivacySwitch.isChecked());

                    // Upload with firebase
                    uploadImageToFirebase(mImageData.getBitmap(), mImageData.getLatLng());


                } else {
                    NetworkUtilities.alertNetworkNotAvailable(AddImageActivity.this);
                }

            }
        });

    }

    private void uploadImageToFirebase(Bitmap bitmap, LatLng latLng) {
        if (mPrivacySwitch.isChecked()) {

            FirebaseUploadPrivateFast firebaseUploadPrivateFast =
                    new FirebaseUploadPrivateFast(this);

            // this uploader is better written, so don't need to manually initialise
            firebaseUploadPrivateFast.uploadImage(bitmap, latLng);


        } else {

            // initialise the upload class
            FirebaseUploadPublicFast firebaseUploadPublicFast =
                    new FirebaseUploadPublicFast(this);
            firebaseUploadPublicFast.initialisingVariables();

            // upload the image
            firebaseUploadPublicFast.uploadImage(bitmap, latLng);
        }
    }


    //.......................................................


    // Firebase upload call back ////////////////////////////////////////////////////
    @Override
    public void onUploadPrivateSuccess() {
        uploadSuccessBackToMap();
    }

    @Override
    public void onUploadPrivateFail() {
        UploadFailBackToMap();
    }

    @Override
    public void onUploadPublicSuccess() {
        uploadSuccessBackToMap();
    }

    @Override
    public void onUploadPublicFail() {
        UploadFailBackToMap();
    }


    private void uploadSuccessBackToMap() {
        // show loading icon
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressText.setVisibility(View.INVISIBLE);

        // show dialog box, asking if people want to exit app.
        new AlertDialog.Builder(AddImageActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Upload Success")
                .setMessage("Congrats! Your photo is uploaded!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMapLayout.setVisibility(View.INVISIBLE);
                        onBackPressed();
                    }
                })
                .show();

    }

    private void UploadFailBackToMap() {
        // show loading icon
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressText.setVisibility(View.INVISIBLE);


        // show dialog box, asking if people want to exit app.
        new AlertDialog.Builder(AddImageActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Upload Failed")
                .setMessage("Something went wrong! Connection to server was interrupted. Try again later.")
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMapLayout.setVisibility(View.INVISIBLE);
                        onBackPressed();
                    }
                })
                .show();

    }


    // .......................................................................


    /// change the functionality of the back button
    @Override
    public void onBackPressed() {
        if (mMapLayout.getVisibility() == View.VISIBLE
                && mConfirmPhotoLayout.getVisibility() == View.INVISIBLE) {
            beginLoadImagePhase();
        } else {
            super.onBackPressed();
        }
    }


    // TO reduce map leak ///////////////////////////////////////////////////////
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
    /////////////////////////////////////////////////////////////////////

}
