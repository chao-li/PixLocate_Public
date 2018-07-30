package clidev.pixlocate.Utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import clidev.pixlocate.Keys.RequestCodes;
import timber.log.Timber;

public class constantLocationUtilities {

    //Context mContext;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private LocationListenerHandler mLocationListenerHandler;


    public interface LocationListenerHandler {
        void OnLocationChanged(Location location);
    }

    public constantLocationUtilities(Context context, LocationListenerHandler locationListenerHandler) {
       // mContext = context.getApplicationContext();
        mLocationListenerHandler = locationListenerHandler;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }



    ////////////////////////////////////////////////////////////
    ////// LOCATION TRACKING SETUP/
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setupLocationUpdate(Activity activity) {
        /// Location stuff
        buildLocationRequest();

        getLastKnownLocation(activity);

        startLocationCallback();


    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(30000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(50);


    }



    public void getLastKnownLocation(Activity activity) {
        // check if permission is allowed
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Ask for permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.FINE_LOCATION_REQUEST_CODE);

            } else {
                // begin tracking
                getOneLocationUpdate(activity);
            }
        } else {
            // begin tracking
            getOneLocationUpdate(activity);
        }




    }

    @SuppressLint("MissingPermission")
    private void getOneLocationUpdate(Activity activity) {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            mLocationListenerHandler.OnLocationChanged(location);
                        }
                    }
                });
    }

    private void startLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    Timber.d("Location: " + location.getLatitude() + ", " + location.getLongitude());
                    mLocationListenerHandler.OnLocationChanged(location);
                }
            }
        };
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////




    // public access methods ////////////////////////////////

    public void beginRequestingLocationUpdates(final Activity activity) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(activity);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Timber.d("Location service is turned on correctly");
                initiateConstateTracking(activity);


            }
        });

        task.addOnFailureListener(activity, new OnFailureListener() {
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
                        resolvable.startResolutionForResult(activity, RequestCodes.REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }

    private void initiateConstateTracking(Activity activity) {
        // check if permission is allowed
        if (Build.VERSION.SDK_INT > 23) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Ask for permission
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RequestCodes.FINE_LOCATION_REQUEST_CODE);

            } else {
                // begin tracking
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        Looper.myLooper());
            }
        } else {
            // begin tracking
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper());
        }
    }

    public void stopRequestingLocationupdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    public void stopLocationCallback() {
        mLocationCallback = null;
    }

}
