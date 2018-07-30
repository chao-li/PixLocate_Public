package clidev.pixlocate.MapSearchFunctions;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import clidev.pixlocate.R;
import clidev.pixlocate.Utilities.MapZoomFactorUtil;

public final class MarkerAndCameraControl {


    // Method for creating marker on the map
    public static LatLng createMarker(Address address, Location location, LatLng latLng, GoogleMap map) {
        LatLng markerLocation = null;

        if (address != null) {
            markerLocation = new LatLng(address.getLatitude(), address.getLongitude());
        }

        if (location != null) {
            markerLocation = new LatLng(location.getLatitude(), location.getLongitude());
        }

        if (latLng != null) {
            markerLocation = latLng;
        }

        // create marker
        Marker marker = map.addMarker(new MarkerOptions()
                .position(markerLocation)
                .title("Requested location!"));
        marker.showInfoWindow();

        return markerLocation;
    }

    public static void animateToMarkerPosition(LatLng markerLocation, GoogleMap map) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, MapZoomFactorUtil.MAP_ZOOM));
    }

}
