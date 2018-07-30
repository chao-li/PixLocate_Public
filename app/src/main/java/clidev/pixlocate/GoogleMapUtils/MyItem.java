package clidev.pixlocate.GoogleMapUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle = null;
    private String mSnippet = null;
    private FirebaseImageWithLocation mFirebaseImageWithLocation;

    public MyItem(double lat, double lng, FirebaseImageWithLocation firebaseImageWithLocation) {
        mPosition = new LatLng(lat, lng);
        mFirebaseImageWithLocation = firebaseImageWithLocation;
    }

    public MyItem(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public FirebaseImageWithLocation getFirebaseImageWithLocation() {
        return mFirebaseImageWithLocation;
    }

    public void setFirebaseImageWithLocation(FirebaseImageWithLocation firebaseImageWithLocation) {
        mFirebaseImageWithLocation = firebaseImageWithLocation;
    }
}