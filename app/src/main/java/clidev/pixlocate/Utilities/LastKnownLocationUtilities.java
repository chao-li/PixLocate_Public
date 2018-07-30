package clidev.pixlocate.Utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import clidev.pixlocate.Activities.MainAppActivity;
import clidev.pixlocate.Keys.RequestCodes;
import timber.log.Timber;




public class LastKnownLocationUtilities {
    public static final String GALLERY = "GALLERY";

    private Context mContext;
    private Fragment mFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationListenerHandler mLocationListenerHandler;

    //private Boolean isLocationObtained = false;

    public interface LocationListenerHandler {
        void OnLocationChanged(Location location);
        void OnLocationFailed();
    }


    public LastKnownLocationUtilities(Context context, Fragment fragment, LocationListenerHandler locationListenerHandler) {
        mContext = context;
        mFragment = fragment;
        mLocationListenerHandler = locationListenerHandler;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
    }




////////////////////////////////////////////////////////////////////////////////////////////////
    public void setupLocationUpdate() {

        getLastKnownLocation();

    }



    private void getLastKnownLocation() {
        // check if permission is allowed
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ){

                // Ask for permission
                mFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.FINE_LOCATION_REQUEST_CODE);

            } else {
                // begin tracking
                getOneLocationUpdate();
            }
        } else {
            // begin tracking
            getOneLocationUpdate();
        }

    }

    @SuppressLint("MissingPermission")
    private void getOneLocationUpdate() {
        // Request Location updates from gps
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                            // pass location data back to MainAppActivity
                        if (location != null) {
                            Timber.d("Location obtained from last known location");
                            //isLocationObtained = true;
                            mLocationListenerHandler.OnLocationChanged(location);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // put methods here to inform the user that no current location is found.
                mLocationListenerHandler.OnLocationFailed();
            }
        });

    }




}
