package clidev.pixlocate.FirebaseUtilities.Delete;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.FirebaseContract;
import timber.log.Timber;

public class FirebaseDeletePublicImage {

    public static final String ALL_USER_EXIST = "ALL_USER_EXIST";
    public static final String GEO_EXIST = "GEO_EXIST" ;
    public static final String PUBLIC_EXIST = "PUBLIC_EXIST";
    public static final String IMAGE_EXIST = "IMAGE_EXIST";
    public static final String SMALL_IMAGE_EXIST = "SMALL_IMAGE_EXIST";
    public static final String GEO_EACH_USER_EXIST = "GEO_EACH_USER_EXIST";


    private FirebaseImageWithLocation mDeleteData;
    private String mUserUniqueKey;
    private String mImageKey;
    private String mImageUrl;
    private String mSmallImageUrl;

    private DeletePublicImageHandler mDeletePublicImageHandler;


    public interface DeletePublicImageHandler {
        void onPublicImageDeleteSuccess(FirebaseImageWithLocation firebaseImageWithLocation);
        void onPublicImageDeleteFail(FirebaseImageWithLocation firebaseImageWithLocation,
                                     String failReason);
    }

    public FirebaseDeletePublicImage(DeletePublicImageHandler deletePublicImageHandler) {
        mDeletePublicImageHandler = deletePublicImageHandler;
    }


    public void deleteImage(FirebaseImageWithLocation firebaseImageWithLocation) {
        mDeleteData = firebaseImageWithLocation;
        mUserUniqueKey = firebaseImageWithLocation.getUserId();
        mImageKey = firebaseImageWithLocation.getImageKey();
        mImageUrl = firebaseImageWithLocation.getImageUrl();
        mSmallImageUrl = firebaseImageWithLocation.getSmallImageUrl();


        deleteGeoFireDataWithListener();

    }



///////////////////////////////////////////////////////
    private void deleteGeoFireDataWithListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                .child(FirebaseContract.ImageGeofireDatabase.PUBLIC)
                .child(mImageKey).removeValue()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // let user know upload failed.
                        Timber.d("Delete step 1: geofire - failed");

                        checkIfGeoDataStillExist();

                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Timber.d("Delete step 1: geofire - success");
                        deleteFromEachUserGeoFireDataWithListener();

                    }
                });
    }


    private void deleteFromEachUserGeoFireDataWithListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                .child(FirebaseContract.ImageGeofireDatabase.EACH_USER)
                .child(mUserUniqueKey)
                .child(mImageKey).removeValue()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // let user know upload failed.
                        Timber.d("Delete step 1.5: geofire - failed");

                        checkIfEachUserGeoDataStillExist();

                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Timber.d("Delete step 1.5: geofire - success");
                        deleteFromPublicImageWithListener();

                    }
                });
    }



    private void deleteFromPublicImageWithListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.ImageDatabase.PUBLIC_IMAGES)
                .child(mImageKey)
                .child(mUserUniqueKey).removeValue()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        checkIfPublicImageStillExist();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Timber.d("Delete step 2: public image - success");
                        deleteImageFromStorageWithListener();
                    }
                });
    }



    private void deleteImageFromStorageWithListener() {
        FirebaseStorage.getInstance().getReferenceFromUrl(mImageUrl).delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        checkIfImageStillExist();

                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Timber.d("Delete step 3: image - success");
                        deleteSmallImageFromStorageWithListener();

                    }
                });

    }

    private void deleteSmallImageFromStorageWithListener() {
        FirebaseStorage.getInstance().getReferenceFromUrl(mSmallImageUrl).delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        checkIfSmallImageStillExist();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Timber.d("Delete step 4: small image - success");
                        deleteFromAllUserDatabaseWithListener();

                    }
                });
    }


    private void deleteFromAllUserDatabaseWithListener() {
        FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(mUserUniqueKey)
                .child(mImageKey).removeValue()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("failed to delete all user image reference");
                        mDeletePublicImageHandler.onPublicImageDeleteFail(mDeleteData,ALL_USER_EXIST);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDeletePublicImageHandler.onPublicImageDeleteSuccess(mDeleteData);
                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////



    // CHECK DATA INTEGRITY ////////////////////////////////////////////////////////////////////////
    private void checkIfGeoDataStillExist() {
        // check if geo data still exist
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference()
                        .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                        .child(FirebaseContract.ImageGeofireDatabase.PUBLIC);

        GeoFire geoFire = new GeoFire(ref);

        geoFire.getLocation(mImageKey, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location == null) {
                    // location data have already been destroyed, proceed with next step
                    Timber.d("geofire no longer exist");
                    deleteFromEachUserGeoFireDataWithListener();

                } else {
                    Timber.d("geoFire still exists");
                    mDeletePublicImageHandler.onPublicImageDeleteFail(mDeleteData, GEO_EXIST);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void checkIfEachUserGeoDataStillExist() {
        // check if geo data still exist
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference()
                        .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                        .child(FirebaseContract.ImageGeofireDatabase.EACH_USER)
                        .child(mUserUniqueKey);

        GeoFire geoFire = new GeoFire(ref);

        geoFire.getLocation(mImageKey, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location == null) {
                    // location data have already been destroyed, proceed with next step
                    Timber.d("geofire each user no longer exist");
                    deleteFromPublicImageWithListener();

                } else {
                    Timber.d("geoFire each user still exists");
                    mDeletePublicImageHandler.onPublicImageDeleteFail(mDeleteData, GEO_EACH_USER_EXIST);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void checkIfPublicImageStillExist() {

        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.ImageDatabase.PUBLIC_IMAGES)
                .child(mImageKey)
                .child(mUserUniqueKey);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren() == false) {

                    Timber.d("public image branch no longer exist");
                    deleteImageFromStorageWithListener();

                } else {

                    Timber.d("public image branch still exists");
                    mDeletePublicImageHandler.onPublicImageDeleteFail(mDeleteData, PUBLIC_EXIST);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void checkIfImageStillExist() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mImageUrl);

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Timber.d("image file still exist");
                mDeletePublicImageHandler.onPublicImageDeleteFail(mDeleteData, IMAGE_EXIST);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Timber.d("image not exist from storage");
                deleteSmallImageFromStorageWithListener();
            }
        });


    }


    private void checkIfSmallImageStillExist() {

        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mSmallImageUrl);

        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                Timber.d("small image file still exist");
                mDeletePublicImageHandler.onPublicImageDeleteFail(mDeleteData, SMALL_IMAGE_EXIST);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Timber.d("small image not exist from storage");
                deleteFromAllUserDatabaseWithListener();
            }
        });

    }

}
