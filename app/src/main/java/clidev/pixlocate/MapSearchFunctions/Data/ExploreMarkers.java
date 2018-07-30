package clidev.pixlocate.MapSearchFunctions.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;


public class ExploreMarkers {

    private List<LatLng> mPhotoLatLngList;

    private Marker mRequestMarker;
    private String mRequestMarkerId;

    public ExploreMarkers () {
    }

    // method
    public void clearPhotoLatLng () {
        if (mPhotoLatLngList != null) {
            mPhotoLatLngList.clear();
        }
    }

    public void clearRequestMarker() {
        if (mRequestMarker != null) {
            mRequestMarker = null;
        }

        if (mRequestMarkerId != null) {
            mRequestMarkerId = null;
        }

    }

    // getter and setters

    public void addPhotoLatLng(LatLng latLng) {
        if (mPhotoLatLngList == null) {
            mPhotoLatLngList = new ArrayList<>();
        }
        mPhotoLatLngList.add(latLng);
    }

    public List<LatLng> getPhotoLatLngList() {
        return mPhotoLatLngList;
    }

    public void setPhotoLatLngList(List<LatLng> photoLatLngList) {
        mPhotoLatLngList = photoLatLngList;
    }

    public Marker getRequestMarker() {
        return mRequestMarker;
    }

    public void setRequestMarker(Marker requestMarker) {
        mRequestMarker = requestMarker;
    }

    public String getRequestMarkerId() {
        return mRequestMarkerId;
    }

    public void setRequestMarkerId(String requestMarkerId) {
        mRequestMarkerId = requestMarkerId;
    }
}
