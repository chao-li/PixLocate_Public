package clidev.pixlocate.MapSearchFunctions.Data;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class ImageData {

    private LatLng mLatLng;
    private Bitmap mBitmap;

    public ImageData() {
        mLatLng = null;
        mBitmap = null;
    }


    public void setLatLng(LatLng latLng) {
        mLatLng = latLng;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }


    public LatLng getLatLng() {
        return mLatLng;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void clearImageData() {
        mBitmap = null;
        mLatLng = null;
    }


}
