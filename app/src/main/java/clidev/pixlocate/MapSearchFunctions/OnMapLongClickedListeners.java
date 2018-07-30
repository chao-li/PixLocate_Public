package clidev.pixlocate.MapSearchFunctions;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import timber.log.Timber;

public class OnMapLongClickedListeners {

    private Context mContext;
    private OnMapLongClickHandler mOnMapLongClickHandler;

    // interface
    public interface OnMapLongClickHandler {
        void onMapLongClicked(LatLng latLng);
    }

    // Constructor
    public OnMapLongClickedListeners(Context context, OnMapLongClickHandler onMapLongClickHandler) {
        mContext = context;
        mOnMapLongClickHandler = onMapLongClickHandler;
    }


    // method
    public void setMapLongClickListener(GoogleMap googleMap) {

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Timber.d("Long click received");
                if (latLng != null) {
                    mOnMapLongClickHandler.onMapLongClicked(latLng);
                }
            }
        });
    }


}
