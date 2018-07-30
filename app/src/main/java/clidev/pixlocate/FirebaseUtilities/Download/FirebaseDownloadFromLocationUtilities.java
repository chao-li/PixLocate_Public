package clidev.pixlocate.FirebaseUtilities.Download;

import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.FirebaseContract;
import timber.log.Timber;

public class FirebaseDownloadFromLocationUtilities {

    private int MINIMUM_DISPLAY_NUMBER = 9;
    private static final int MAXIMUM_IMAGE_NUMBERS = 999;
    private LocationQueryHandler mLocationQueryHandler;
    private int searchCount;
    private int publicPhotoCount;
    private Boolean isPublic;


    // interface
    public interface LocationQueryHandler {
        void onImageDataFound(FirebaseImageWithLocation firebaseImageWithLocation, Boolean searchedEntireEarth);
        void onSearchRadiusSet(Double radius);
        void onNoImageFound();
        void onImageRemoved(String key);
    }


    // constructor
    public FirebaseDownloadFromLocationUtilities(LocationQueryHandler locationQueryHandler) {
        mLocationQueryHandler = locationQueryHandler;
    }


    //  Query geofire to get a series of image keys that corresponds to those that are in my location
    public void queryThisLocationForImage(LatLng latLng, Boolean isPublic, int minImageNumber) {
        searchCount = 0;
        this.isPublic = isPublic;
        MINIMUM_DISPLAY_NUMBER = minImageNumber;

        queryThisLocationForDataCount(latLng, useRadius(searchCount));

    }

    private void queryThisLocationForDataCount(final LatLng latLng, final Double radius) {

        DatabaseReference locationReference;
        // reference the location database
        if (isPublic) {
            // query from public location data
            locationReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                    .child(FirebaseContract.ImageGeofireDatabase.PUBLIC);
        } else {
            // query from personal location data.
            locationReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                    .child(FirebaseContract.ImageGeofireDatabase.EACH_USER)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }


        // Create geofire query
        GeoFire geoFire = new GeoFire(locationReference);

        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),
                radius); // 10km radius

        // inform user what range we are searching in.
        mLocationQueryHandler.onSearchRadiusSet(radius);

        // list use to collect keys found, to gauge the number of results in this location
        final List<String> keyList = new ArrayList<>();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                // if key exists add it to the key list
                if (key != null) {
                        keyList.add(key);
                }
            }

            @Override
            public void onKeyExited(String key) {
                // uncount the key
                keyList.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // photo won't be moving, it is a static object

            }

            @Override
            public void onGeoQueryReady() {

                if (keyList.size() < MINIMUM_DISPLAY_NUMBER){
                    // first remove the query listener.
                    removeQueryListener(geoQuery);

                    // expand search radius
                    searchCount++;

                    // if the search radius have not reached the limit of available radiusArray
                    if (searchCount <= radiusArray.length - 1) {
                        // keep doing data count search
                        Timber.d("expanding search to " + useRadius(searchCount) + "km");
                        queryThisLocationForDataCount(latLng, useRadius(searchCount));

                    } else {
                        // just query the entire database
                        queryEntireImageDb();
                    }

                } else { // begin the actual query if minimum photo number have been reached.

                    // first remove the query listener.
                    removeQueryListener(geoQuery);

                    Timber.d("Number of keys are " + keyList.size());
                    Timber.d("Test run complete");

                    // restart query, but this time will output data
                    queryThisLocationWithinRadius(latLng, radius);
                }


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                // inform user that query isnt working. try again later.
                mLocationQueryHandler.onNoImageFound();
            }
        });


    }

    // Querying a location with determined radius

    private void queryThisLocationWithinRadius(final LatLng latLng,
                                               final Double radius) {

        publicPhotoCount = 0;

        DatabaseReference locationReference;
        // reference the location database
        if (isPublic) {
            // query from public location data
            locationReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                    .child(FirebaseContract.ImageGeofireDatabase.PUBLIC);
        } else {
            // query from personal location data.
            locationReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                    .child(FirebaseContract.ImageGeofireDatabase.EACH_USER)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }





        // begin geofire query
        GeoFire geoFire = new GeoFire(locationReference);
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude),
                radius);


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                publicPhotoCount++;
                if (publicPhotoCount <= MAXIMUM_IMAGE_NUMBERS) {
                    outputData(key, location);
                }

            }

            @Override
            public void onKeyExited(String key) {
                // write code to remove this photo from recyclerview adapter
                publicPhotoCount--;
                mLocationQueryHandler.onImageRemoved(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // photo won't be moving, it is a static object
            }

            @Override
            public void onGeoQueryReady() {
                // first remove the query listener.
                removeQueryListener(geoQuery);

                Timber.d("Data collection complete");


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                // inform user that query isnt working. try again later.
                mLocationQueryHandler.onNoImageFound();
            }
        });
    }


    //////////////////////////////////////////////////////////////////


    // Querying entire databasese

    private void queryEntireImageDb() {
        publicPhotoCount = 0;

        mLocationQueryHandler.onSearchRadiusSet(null);

        Timber.d("Querying entire world");

        if (isPublic) {

            queryAllPublicData();

        } else {

            queryAllUserData();


        }


    }

    private void queryAllPublicData() {
        final DatabaseReference publicPhotoReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.PUBLIC_IMAGES);


        publicPhotoReference.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {


                        for (DataSnapshot childSnapshot : singleSnapShot.getChildren()) {
                            publicPhotoCount++;

                            if (publicPhotoCount <= 999) {
                                FirebaseImageWithLocation firebaseImageWithLocation =
                                        childSnapshot.getValue(FirebaseImageWithLocation.class);

                                mLocationQueryHandler.onImageDataFound(firebaseImageWithLocation, true);

                                //Timber.d("Data passed through,, full db search");
                            }
                        }

                    }
                } else {
                    mLocationQueryHandler.onNoImageFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void queryAllUserData() {
        DatabaseReference publicPhotoReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        publicPhotoReference.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {

                        FirebaseImageWithLocation firebaseImageWithLocation =
                                singleSnapShot.getValue(FirebaseImageWithLocation.class);

                        mLocationQueryHandler.onImageDataFound(firebaseImageWithLocation,true);

                        //Timber.d("Data passed through,, full db search");

                    }
                } else {
                    mLocationQueryHandler.onNoImageFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mLocationQueryHandler.onNoImageFound();
            }
        });
    }





















    /// OUTPUTTING DATA ///////////////////////////////////////////////////////////

    private void outputData(final String key, final GeoLocation location) {
        if (isPublic) {
            outputDataForPublicDatabase(key);
        } else {
            outputDataForUserData(key);
        }

    }


    private void outputDataForPublicDatabase(String key) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child("public_images")
                .child(key);

        // query the database for the key that have been found
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                        FirebaseImageWithLocation firebaseImageWithLocation =
                                childSnapshot.getValue(FirebaseImageWithLocation.class);

                        mLocationQueryHandler.onImageDataFound(firebaseImageWithLocation, false);

                        //Timber.d("Data passed through");
                    }


                } else {
                    Timber.d("data null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void outputDataForUserData(String key) {
        DatabaseReference reference_public = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(key);

        // query the database for the key that have been found
        reference_public.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                        FirebaseImageWithLocation firebaseImageWithLocation =
                                dataSnapshot.getValue(FirebaseImageWithLocation.class);

                        mLocationQueryHandler.onImageDataFound(firebaseImageWithLocation, false);

                        //Timber.d("Data passed through");
                } else {
                    Timber.d("data null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mLocationQueryHandler.onNoImageFound();
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////








    private static Double[] radiusArray = new Double[]{
            0.05,
            0.1,
            0.2,
            0.3,
            0.4,
            0.5,
            1d,
            2d,
            3d,
            4d,
            5d,
            10d,
            15d,
            20d,
            50d,
            75d,
            100d,
            200d,
            300d,
            500d,
            1000d,
            1500d,
            2000d,
            5000d};



    private static Double useRadius(int index) {
        return radiusArray[index];
    }

    private void removeQueryListener(GeoQuery geoQuery) {
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
    }


}
