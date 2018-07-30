package clidev.pixlocate.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import clidev.pixlocate.Keys.RequestCodes;
import timber.log.Timber;


/////////////////////
//NO LONGER USED

/////////////////////
public final class GeneralLocationUtilities {





    public static String getCountry (Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String country = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (listAddresses != null && listAddresses.size() > 0) {

                if (listAddresses.get(0).getCountryName() != null) {

                    // update country information
                    country = listAddresses.get(0).getCountryName();

                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return country;
    }

    public static String getCountry (Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String country = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddresses != null && listAddresses.size() > 0) {

                if (listAddresses.get(0).getCountryName() != null) {

                    // update country information
                    country = listAddresses.get(0).getCountryName();

                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return country;
    }



    public static String getCity (Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String city = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (listAddresses != null && listAddresses.size() > 0) {

                if (listAddresses.get(0).getLocality() != null) {
                    city = listAddresses.get(0).getLocality();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return city;
    }


    public static String getCity (Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String city = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddresses != null && listAddresses.size() > 0) {

                if (listAddresses.get(0).getLocality() != null) {
                    city = listAddresses.get(0).getLocality();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return city;
    }




    public static String getPostCode (Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String postCode = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (listAddresses != null && listAddresses.size() > 0) {


                if (listAddresses.get(0).getPostalCode() != null) {
                    postCode = listAddresses.get(0).getPostalCode();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return postCode;
    }

    public static String getPostCode (Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        String postCode = "";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (listAddresses != null && listAddresses.size() > 0) {


                if (listAddresses.get(0).getPostalCode() != null) {
                    postCode = listAddresses.get(0).getPostalCode();
                }

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return postCode;
    }



}

