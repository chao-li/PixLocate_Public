package clidev.pixlocate.FirebaseUtilities.Delete;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.FirebaseContract;
import timber.log.Timber;



public class FirebaseDeletePrivateImage {


    public static final String ALL_USER_EXIST = "ALL_USER_EXIST";
    public static final String GEO_EXIST = "GEO_EXIST" ;
    public static final String IMAGE_EXIST = "IMAGE_EXIST";
    public static final String SMALL_IMAGE_EXIST = "SMALL_IMAGE_EXIST";
    public static final String GEO_EACH_USER_EXIST = "GEO_EACH_USER_EXIST";

    private FirebaseImageWithLocation mDeleteData;
    private String mUserUniqueKey;
    private String mImageKey;
    private String mImageUrl;
    private String mSmallImageUrl;

    private DeletePrivateImageHandler mDeletePrivateImageHandler;


    public interface DeletePrivateImageHandler {
        void onPrivateImageDeleteSuccess(FirebaseImageWithLocation firebaseImageWithLocation);
        void onPrivateImageDeleteFail(FirebaseImageWithLocation firebaseImageWithLocation,
                                      String failReason);

    }


    public FirebaseDeletePrivateImage (DeletePrivateImageHandler deletePrivateImageHandler) {
        mDeletePrivateImageHandler = deletePrivateImageHandler;
    }

    public void deleteImage(FirebaseImageWithLocation firebaseImageWithLocation) {
        mDeleteData = firebaseImageWithLocation;
        mUserUniqueKey = firebaseImageWithLocation.getUserId();
        mImageKey = firebaseImageWithLocation.getImageKey();
        mImageUrl = firebaseImageWithLocation.getImageUrl();
        mSmallImageUrl = firebaseImageWithLocation.getSmallImageUrl();


        deleteFromEachUserGeoFireDataWithListener();

    }


    ////////////////////////////////////////////////////////////////



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
                        mDeletePrivateImageHandler.onPrivateImageDeleteFail(mDeleteData,ALL_USER_EXIST);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDeletePrivateImageHandler.onPrivateImageDeleteSuccess(mDeleteData);
                    }
                });
    }



    ////////////////////////////////////////////////////////////////////////////////////////



    // CHECK DATA INTEGRITY ////////////////////////////////////////////////////////////////////////

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
                    deleteImageFromStorageWithListener();

                } else {
                    Timber.d("geoFire each user still exists");
                    mDeletePrivateImageHandler.onPrivateImageDeleteFail(mDeleteData, GEO_EACH_USER_EXIST);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                mDeletePrivateImageHandler.onPrivateImageDeleteFail(mDeleteData, IMAGE_EXIST);
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
                mDeletePrivateImageHandler.onPrivateImageDeleteFail(mDeleteData, SMALL_IMAGE_EXIST);
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
