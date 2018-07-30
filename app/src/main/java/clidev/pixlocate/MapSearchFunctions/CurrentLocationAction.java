package clidev.pixlocate.MapSearchFunctions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnSuccessListener;

import clidev.pixlocate.Keys.RequestCodes;

public class CurrentLocationAction {

    private Context mContext;
    private Fragment mFragment;

    private CurrentLocationHandler mCurrentLocationHandler;

    // interface
    public interface CurrentLocationHandler{
        void onCurrentLocationResult(Location location);
    }


    // Constructor
    public CurrentLocationAction (Context context, CurrentLocationHandler currentLocationHandler) {
        mContext = context;
        mFragment = null;
        mCurrentLocationHandler = currentLocationHandler;
    }

    public CurrentLocationAction (Context context, Fragment fragment, CurrentLocationHandler currentLocationHandler) {
        mContext = context;
        mFragment = fragment;
        mCurrentLocationHandler = currentLocationHandler;
    }


    // methods
    public void enableMoveToUserLocationButton(GoogleMap googleMap) {
        // For showing a move to my location button
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Ask for permission
                if (mFragment != null) { // if fragment exist, use the fragment's method of asking for permission
                    mFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            RequestCodes.FINE_LOCATION_REQUEST_CODE);
                } else { // if fragment don't exist, use the activity's method of asking for permission
                    ActivityCompat.requestPermissions((Activity) mContext,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            RequestCodes.FINE_LOCATION_REQUEST_CODE);
                }

            } else {
                googleMap.setMyLocationEnabled(true);
                setMoveToUserLocationButtonListener(googleMap);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
            setMoveToUserLocationButtonListener(googleMap);
        }

    }

    private void setMoveToUserLocationButtonListener(GoogleMap googleMap) {
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED ){

                        // Ask for permission
                        if (mFragment != null) {
                            mFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    RequestCodes.FINE_LOCATION_REQUEST_CODE);
                        } else {
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    RequestCodes.FINE_LOCATION_REQUEST_CODE);
                        }
                    } else {
                        getLastKnownLocation();

                    }
                } else {
                    // begin tracking
                    getLastKnownLocation();
                }

                return true;
            }

            @SuppressLint("MissingPermission")
            private void getLastKnownLocation() {
                // begin tracking
                // Request Location updates from gps
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener((Activity) mContext, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                // pass location data back to MainAppActivity
                                if (location != null) {

                                    mCurrentLocationHandler.onCurrentLocationResult(location);

                                }
                            }
                        });
            }
        });
    }

}
