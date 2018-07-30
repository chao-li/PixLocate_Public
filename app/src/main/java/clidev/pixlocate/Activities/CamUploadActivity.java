package clidev.pixlocate.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.FirebaseUtilities.Upload.FirebaseUploadPrivateFast;
import clidev.pixlocate.FirebaseUtilities.Upload.FirebaseUploadPublicFast;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.R;
import clidev.pixlocate.Utilities.CompressBitmapUtil;
import clidev.pixlocate.Utilities.LastKnownLocationUtilities;
import clidev.pixlocate.Utilities.constantLocationUtilities;
import clidev.pixlocate.Utilities.CameraKitUtility;
import clidev.pixlocate.Utilities.InfoUtilities;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class CamUploadActivity extends AppCompatActivity implements View.OnClickListener,
        CameraKitUtility.CameraFunctionHandler,
        FirebaseUploadPrivateFast.FirebasePrivateUploadHandler,
        FirebaseUploadPublicFast.FirebasePublicUploadHandler,
        LocationListener{


    private Location mCurrentLocation;
    private Toast mToast;
    private Boolean isCameraMode;
    private Boolean canTakePicture;
    private Bitmap mBitmap;
    private Boolean isCamFacingFront;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationManager mLocationManager;


    @BindView(R.id.cam_activity_cameraview)
    CameraView mCameraView;
    @BindView(R.id.cam_activity_cam_trigger)
    ImageView mCameraTrigger;
    @BindView(R.id.cam_activity_confirm_layout)
    ConstraintLayout mConfirmPhotoLayout;
    @BindView(R.id.cam_activity_confirm_image)
    ImageView mConfirmPhotoImageView;
    @BindView(R.id.cam_activity_upload_progress)
    ProgressBar mUploadProgress;
    @BindView(R.id.cam_activity_progress_text)
    TextView mUploadProgressTextView;
    @BindView(R.id.cam_activity_upload_button)
    ImageView mUploadButton;
    @BindView(R.id.cam_activity_cancel_button)
    ImageView mCancelButton;
    @BindView(R.id.cam_activity_privacy_switch)
    Switch mPrivacySwitch;
    @BindView(R.id.cam_activity_private_text)
    TextView mPrivateText;
    @BindView(R.id.cam_activity_public_text)
    TextView mPublicText;
    @BindView(R.id.cam_activity_cam_layout)
    ConstraintLayout mCameraLayout;
    @BindView(R.id.cam_activity_flash_off)
    ImageView mFlashOff;
    @BindView(R.id.cam_activity_flash_on)
    ImageView mFlashOn;
    @BindView(R.id.cam_activity_cam_rotate)
    ImageView mCamRotate;
    @BindView(R.id.cam_activity_info)
    FloatingActionButton mInfo;
    @BindView(R.id.cam_activity_rotate_left) ImageView mRotateLeft;
    @BindView(R.id.cam_activity_rotate_right) ImageView mRotateRight;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // if it was the location request
        if (requestCode == RequestCodes.FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Take user back to previous activity
                    onBackPressed();
                    //Intent intent = new Intent(CameraActivity.this, MainAppActivity.class);
                    //startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Timber.d("GPS enabled");
            } else if (resultCode == RESULT_CANCELED) {
                Timber.d("GPS no enabled, taking user back to previous activity");

                Intent intent = new Intent(CamUploadActivity.this, MainAppActivity.class);
                finish();
                startActivity(intent);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_upload);

        ButterKnife.bind(this);

        setTitle("Camera");

        // setting inital values
        isCameraMode = true;
        canTakePicture = false;
        isCamFacingFront = false;

        // setting on button clicked listener
        mCameraTrigger.setOnClickListener(this);
        mUploadButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mFlashOff.setOnClickListener(this);
        mFlashOn.setOnClickListener(this);
        mFlashOff.setOnClickListener(this);
        mCamRotate.setOnClickListener(this);
        mRotateLeft.setOnClickListener(this);
        mRotateRight.setOnClickListener(this);

        // set camera event listener
        mCameraView.addCameraKitListener(new CameraKitUtility(this));

        // set info floating action button action
        setInfoFloatingActionButton();

        // get last known location
        SetCallbackForLastKnownLocation();

        // Fused Location Provider Client tracking method.
        setConstantTracking();
    }

    private void setInfoFloatingActionButton() {

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // show dialog box, asking if people want to exit app.
                new AlertDialog.Builder(CamUploadActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Hint")
                        .setMessage(InfoUtilities.cameraInfo())
                        .setPositiveButton("Ok", null)
                        .show();

            }
        });
    }


    // LOCATION SERVICES/////////////////////////////////
    private void SetCallbackForLastKnownLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        RequestCodes.FINE_LOCATION_REQUEST_CODE);

            } else {
                getLastKnownLocation();

            }
        } else {
            // begin tracking
            getLastKnownLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        // begin tracking
        // Request Location updates from gps
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        // pass location data back to MainAppActivity
                        if (location != null) {
                            mCurrentLocation = location;
                            Timber.d("First Location Update");
                            Timber.d("Latitude: " + mCurrentLocation.getLatitude());
                            Timber.d("Longitude: " + mCurrentLocation.getLongitude());

                        }
                    }
                });
    }

    private void setConstantTracking() {
        // setup fused location provider
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // build location request
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(50);

        // Setup the callback function.
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...

                    mCurrentLocation = location;
                }
            }
        };
    }


    ///////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();

        // start camera
        if (isCameraMode) {
            mCameraView.start();
        }


        // Fused location provider client method
        checkGpsAndStartTracking();

        // Location manager method.
        //startLocationListener();


    }


    @Override
    protected void onPause() {
        super.onPause();

        // stop camera
        mCameraView.stop();

        // Fused location provider client method
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);



        // Location manager method
        /*
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
        */
    }



    private void startLocationListener() {
        // Checking if location permission is allowed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(CamUploadActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.FINE_LOCATION_REQUEST_CODE);
            return;
        }

        // checking if gps is turned on
        boolean gps_enabled = false;

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled) {
            Timber.d("Gps is disabled");
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("GPS is not enabled. Please turn on your GPS to \"High Accuracy Mode\"");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Hell Naw", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    onBackPressed();
                }
            });
            dialog.show();

        } else {
            Timber.d("Gps is already enabled");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 50, this);

        }
    }

    private void checkGpsAndStartTracking() {
        // first check location setting, if phone can receive gps
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Timber.d("Location service is turned on correctly");
                InitiateLocationCallback();


            }

            private void InitiateLocationCallback() {
                // check if permission is allowed
                if (Build.VERSION.SDK_INT > 23) {
                    if (ContextCompat.checkSelfPermission(CamUploadActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Ask for permission
                        ActivityCompat.requestPermissions(CamUploadActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.FINE_LOCATION_REQUEST_CODE);

                    } else {
                        // begin tracking
                        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback,
                                Looper.myLooper());
                    }
                } else {
                    // begin tracking
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper());
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        Timber.d("Location service isn't turned on correcntly");
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(CamUploadActivity.this, RequestCodes.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    } // code for fused location provider client



    // Location update listener ////////////////////////////////////////////////
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Timber.d("Location updated");
        Timber.d("Latitude: " + mCurrentLocation.getLatitude());
        Timber.d("Longitude: " + mCurrentLocation.getLongitude());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /////////////////////////////////////////////////////////////




    // When buttons are clicked.
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cam_activity_cam_trigger:
                capturePhotoIfReady();
                break;

            case R.id.cam_activity_upload_button:
                if (NetworkUtilities.isNetworkAvailable(this)) {
                    // hide upload, cancel and privacy switch
                    hideUploadCancelSwitch();

                    // display loading icon
                    showProgressBar();


                    uploadImageToFirebase();
                } else {
                    // network alert dialog
                    NetworkUtilities.alertNetworkNotAvailable(this);
                }

                break;

            case R.id.cam_activity_cancel_button:

                backToCameraMode();
                break;


            case R.id.cam_activity_flash_off:

                // turn flash on
                mFlashOff.setVisibility(View.INVISIBLE);
                mFlashOn.setVisibility(View.VISIBLE);
                mCameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
                mCameraView.setFlash(CameraKit.Constants.FLASH_ON);
                Timber.d("flash off clicked");
                break;

            case R.id.cam_activity_flash_on:
                mFlashOn.setVisibility(View.INVISIBLE);
                mFlashOff.setVisibility(View.VISIBLE);
                mCameraView.setMethod(CameraKit.Constants.METHOD_STILL);
                mCameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                Timber.d("flash on clicked");
                break;

            case R.id.cam_activity_cam_rotate:
                if (isCamFacingFront == false) {
                    mCameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    isCamFacingFront = true;
                } else {
                    mCameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    isCamFacingFront = false;
                }
                break;

            case R.id.cam_activity_rotate_left:
                Timber.d("clicked rotate left");

                Bitmap bitmapLeft = rotateBitmap(mBitmap, -90);

                Glide.with(CamUploadActivity.this).load(bitmapLeft).into(mConfirmPhotoImageView);

                mBitmap = bitmapLeft;
                break;

            case R.id.cam_activity_rotate_right:
                Timber.d("clicked rotate left");

                Bitmap bitmapRight = rotateBitmap(mBitmap, 90);

                Glide.with(CamUploadActivity.this).load(bitmapRight).into(mConfirmPhotoImageView);

                mBitmap = bitmapRight;
                break;


                default:
                    break;
        }

    }




    private void capturePhotoIfReady() {
        if (canTakePicture == true) {
            if (mCurrentLocation != null) {
                Timber.d("taking image");
                mCameraView.captureImage();
            } else {
                Utilities.cancelToast(mToast);
                mToast.makeText(this, "Location not received, please make sure your GPS is turned on!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void hideUploadCancelSwitch() {
        mUploadButton.setVisibility(View.INVISIBLE);
        mCancelButton.setVisibility(View.INVISIBLE);
        mPrivacySwitch.setVisibility(View.INVISIBLE);
        mPublicText.setVisibility(View.INVISIBLE);
        mPrivateText.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        mUploadProgress.setVisibility(View.VISIBLE);
        mUploadProgressTextView.setVisibility(View.VISIBLE);
    }

    private void uploadImageToFirebase() {
        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        if (mPrivacySwitch.isChecked()) {

            FirebaseUploadPrivateFast firebaseUploadPrivateFast =
                    new FirebaseUploadPrivateFast(this);

            // this uploader is better written, so don't need to manually initialise
            firebaseUploadPrivateFast.uploadImage(mBitmap, latLng);


        } else {

            // initialise the upload class
            FirebaseUploadPublicFast firebaseUploadPublicFast =
                    new FirebaseUploadPublicFast(this);
            firebaseUploadPublicFast.initialisingVariables();

            // upload the image
            firebaseUploadPublicFast.uploadImage(mBitmap, latLng);
        }
    }

    private void backToCameraMode() {
        mCameraView.start();
        isCameraMode = true;

        // show cameras
        mCameraLayout.setVisibility(View.VISIBLE);

        // hide confirm image layout
        mConfirmPhotoLayout.setVisibility(View.INVISIBLE);
        hideProgressBar();


        // show upload or cancel buttons and privacy switch
        mUploadButton.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.VISIBLE);
        mPrivacySwitch.setVisibility(View.VISIBLE);
        mPublicText.setVisibility(View.VISIBLE);
        mPrivateText.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        // hide progress bars
        mUploadProgressTextView.setVisibility(View.INVISIBLE);
        mUploadProgress.setVisibility(View.INVISIBLE);
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    /////////////////////////////////////////////////////////////////////



    // Camera event listener call back /////////////////////////////////////////////////////////////////////
    @Override
    public void onIsCameraReady(Boolean isReady) {
        canTakePicture = isReady;
    }


    @Override
    public void onImageCaptured(CameraKitImage cameraKitImage) {
        goToConfirmImageMode(cameraKitImage);

    }

    private void goToConfirmImageMode(CameraKitImage cameraKitImage) {
        mBitmap = CompressBitmapUtil.getResizedBitmap(cameraKitImage.getBitmap(), 500);

        // turn off camera
        mCameraView.stop();
        isCameraMode = false;

        // hide camera layout
        mCameraLayout.setVisibility(View.INVISIBLE);

        // show confirm image layout
        mConfirmPhotoLayout.setVisibility(View.VISIBLE);

        // display captured photo
        Glide.with(CamUploadActivity.this).load(mBitmap).into(mConfirmPhotoImageView);
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // firebase upload callback///////////////////////////////////////////////////////////////////////////
    @Override
    public void onUploadPrivateSuccess() {
        uploadSuccessBackToCam();

    }

    @Override
    public void onUploadPrivateFail() {
        uploadFailBackToCam();

    }



    @Override
    public void onUploadPublicSuccess() {
        uploadSuccessBackToCam();
    }

    @Override
    public void onUploadPublicFail() {
        uploadFailBackToCam();

    }


    private void uploadSuccessBackToCam() {
        // code for when photo is successfully uploaded
        backToCameraMode();
    }

    private void uploadFailBackToCam() {
        // code for when photo failed to upload
        backToCameraMode();
        Utilities.cancelToast(mToast);
        mToast.makeText(this, "Something's wrong, could not decode image to upload! Try taking another photo!", Toast.LENGTH_SHORT).show();
    }




    /////////////////////////////////////////////////////////////////////////////////////////////////////////



}
