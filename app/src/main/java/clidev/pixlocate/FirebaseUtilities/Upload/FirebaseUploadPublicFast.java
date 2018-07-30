package clidev.pixlocate.FirebaseUtilities.Upload;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.FirebaseContract;
import timber.log.Timber;

public class FirebaseUploadPublicFast {

    private static final String IMAGE_BYTE = "IMAGE_BYTE";
    private static final String SMALL_IMAGE_BYTE = "SMALL_IMAGE_BYTE";

    // firebase variables
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mPublicImageDbReference;
    DatabaseReference mAllUserImageReference;
    DatabaseReference mImageUniqueNameReference;
    DatabaseReference mGeoFireReference;

    FirebaseStorage mFirebaseStorage;
    StorageReference mPublicImageReference;
    StorageReference mPrivateImageReference;
    StorageReference mPublicSmallImageReference;
    StorageReference mPrivateSmallImageReference;

    FirebaseAuth mFirebaseAuth;

    // member variables
    private LatLng mLatLng;

    private byte[] mByteArray;
    private byte[] mSmallByteArray;

    private String mImageUniqueName;
    private UploadTask mUploadImageTask;
    private StorageReference mImageReference;
    private String mImageDownloadUrl;

    private String mSmallImageUniqueName;
    private UploadTask mUploadSmallImageTask;
    private StorageReference mSmallImageReference;
    private String mSmallImageDownloadUrl;

    private long mImageUploadTime;

    // interface variables
    private FirebasePublicUploadHandler mFirebasePublicUploadHandler;



    // interface
    public interface FirebasePublicUploadHandler {
        void onUploadPublicSuccess();
        void onUploadPublicFail();
    }

    // Constructor
    public FirebaseUploadPublicFast(FirebasePublicUploadHandler firebasePublicUploadHandler) {
        mFirebasePublicUploadHandler = firebasePublicUploadHandler;
    }


    // methods

    private void clearVariables() {

    }

    public void initialisingVariables() {

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mPublicImageDbReference = mFirebaseDatabase.getReference()
                .child(FirebaseContract.ImageDatabase.PUBLIC_IMAGES);

        mAllUserImageReference = mFirebaseDatabase.getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES);

        mImageUniqueNameReference = mFirebaseDatabase.getReference()
                .child(FirebaseContract.ImageDatabase.IMAGE_UNIQUE_NAME);

        mGeoFireReference = mFirebaseDatabase.getReference()
                .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION);

        mPublicImageReference = mFirebaseStorage.getReference()
                .child(FirebaseContract.ImageStorage.PUBLIC_IMAGE_FOLDER);

        mPrivateImageReference = mFirebaseStorage.getReference()
                .child(FirebaseContract.ImageStorage.PRIVATE_IMAGE_FOLDER);

        mPublicSmallImageReference = mFirebaseStorage.getReference()
                .child(FirebaseContract.ImageStorage.PUBLIC_SMALL_IMAGE_FOLDER);

        mPrivateSmallImageReference = mFirebaseStorage.getReference()
                .child(FirebaseContract.ImageStorage.PRIVATE_SMALL_IMAGE_FOLDER);

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void uploadImage(Bitmap bitmap, LatLng latLng) {
        // saving passed in location data.
        mLatLng = latLng;

        if (mLatLng == null || bitmap == null) {
            mFirebasePublicUploadHandler.onUploadPublicFail();
        } else {
            new ImageUploadTask().execute(bitmap);
        }

    }




    public class ImageUploadTask extends AsyncTask<Bitmap, Void,  Map<String, byte[]>> {

        @Override
        protected  Map<String, byte[]> doInBackground(Bitmap... bitmaps) {
            // update the location details

            try {
                // get slightly smaller image
                Bitmap bitmap = bitmaps[0];

                // Compress bitmap to png
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                stream.close();


                // get smaller bitmap
                Bitmap smallerBitmap = getResizedBitmap(bitmaps[0], 150);

                ByteArrayOutputStream smallStream = new ByteArrayOutputStream();
                smallerBitmap.compress(Bitmap.CompressFormat.PNG, 100, smallStream);
                byte[] smallByteArray = smallStream.toByteArray();
                smallStream.close();

                Map<String, byte[]> imageByteMap = new HashMap<>();

                imageByteMap.put(IMAGE_BYTE, byteArray);
                imageByteMap.put(SMALL_IMAGE_BYTE, smallByteArray);


                return imageByteMap;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Map<String, byte[]> imageByteMap) {
            // logic for uploading onto firebase storage
            if (imageByteMap != null) {

                mByteArray = imageByteMap.get(IMAGE_BYTE);
                mSmallByteArray = imageByteMap.get(SMALL_IMAGE_BYTE);

                Timber.d("image compression success, uploading images");

                uploadPublicSmallImage();

            } else {

                // Inform user that the upload has failed
                mFirebasePublicUploadHandler.onUploadPublicFail();
            }
        }
    }




    // firebase code for uploading images to storage ////////////////////////////////////////////
    private void uploadPublicSmallImage() {

        mSmallImageUniqueName = getUniqueName();

        mSmallImageReference = mPublicSmallImageReference
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(mSmallImageUniqueName);

        mUploadSmallImageTask = mSmallImageReference.putBytes(mSmallByteArray);


        mUploadSmallImageTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Timber.d("Upload small image failed");

                mFirebasePublicUploadHandler.onUploadPublicFail();


            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ..

                Timber.d("Upload small image success");

                // if uploading small image is successful, upload big image
                uploadPublicImage();

            }
        });
    }




    private void uploadPublicImage() {
        // creating unique image name

        mImageUniqueName = getUniqueName();

        mImageReference = mPublicImageReference
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(mImageUniqueName);



        mUploadImageTask = mImageReference.putBytes(mByteArray);

        mUploadImageTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Timber.d("Upload image failed");

                mFirebasePublicUploadHandler.onUploadPublicFail();



            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...

                Timber.d("Upload image success, begin uploading small image");


                // get download url of small image
                getSmallImageDownloadUrl(mUploadSmallImageTask, mSmallImageReference);

                // quick release
                mFirebasePublicUploadHandler.onUploadPublicSuccess();


            }
        });
    }



    private void getSmallImageDownloadUrl(UploadTask uploadTask, final StorageReference imageReference) {
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();

                    mSmallImageDownloadUrl = downloadUrl.toString();
                    Timber.d("Small image download url is: " + mSmallImageDownloadUrl);


                    // if uploading image is successful, grab bigImageUrl
                    getImageDownloadUrl(mUploadImageTask, mImageReference);



                } else {

                    // Handle failures
                    // ...

                    //mFirebasePublicUploadHandler.onUploadPublicFail();


                }
            }
        });
    }



    private void getImageDownloadUrl(UploadTask uploadTask, final StorageReference imageReference) {
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();

                    mImageDownloadUrl = downloadUrl.toString();
                    Timber.d("Image download url is: " + mImageDownloadUrl);



                    getImageUploadTimeStamp();

                } else {

                    // Handle failures
                    // ...

                    //mFirebasePublicUploadHandler.onUploadPublicFail();


                }
            }
        });
    }


    private void getImageUploadTimeStamp() {
        if (mImageDownloadUrl != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(mImageDownloadUrl);

            imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    if (storageMetadata != null) {

                        mImageUploadTime = storageMetadata.getCreationTimeMillis();

                        // now upload image info data to database
                        uploadImageInfoToDatabase();


                    } else {
                         //mFirebasePublicUploadHandler.onUploadPublicFail();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //mFirebasePublicUploadHandler.onUploadPublicFail();
                }
            });
        }



    }


    ////////////////////////////////////////////////////////////////////////////


    // firebase code for uploading image info to Database
    private void uploadImageInfoToDatabase() {
        // first get the image unique key
        final String imageDataUniqueKey = mAllUserImageReference
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(FirebaseContract.ImageDatabase.PUBLIC)
                .push().getKey();

        final FirebaseImageWithLocation firebaseImageWithLocation = new FirebaseImageWithLocation(
                mFirebaseAuth.getCurrentUser().getUid(),
                mFirebaseAuth.getCurrentUser().getDisplayName(),
                false,
                mImageDownloadUrl,
                mSmallImageDownloadUrl,
                mImageUniqueName,
                mSmallImageUniqueName,
                mLatLng.latitude,
                mLatLng.longitude,
                imageDataUniqueKey,
                mImageUploadTime);



        // Uploading to all user database
        mAllUserImageReference
                .child(mFirebaseAuth.getCurrentUser().getUid())
                .child(imageDataUniqueKey)
                .setValue(firebaseImageWithLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // only upload to public if already uploaded to personal.
                uploadPublicPhotoInfo(imageDataUniqueKey, firebaseImageWithLocation);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //mFirebasePublicUploadHandler.onUploadPublicFail();

            }
        });



    }

    private void uploadPublicPhotoInfo(final String imageDataUniqueKey, FirebaseImageWithLocation firebaseImageWithLocation) {
        mPublicImageDbReference
                .child(imageDataUniqueKey)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(firebaseImageWithLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                uploadGeoLocationInfo(imageDataUniqueKey);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.d("image info upload failed");


               //mFirebasePublicUploadHandler.onUploadPublicFail();


            }
        });
    }

    private void uploadGeoLocationInfo(final String imageDataUniqueKey) {
        GeoFire geoFire = new GeoFire(mGeoFireReference
                .child(FirebaseContract.ImageGeofireDatabase.PUBLIC));


        geoFire.setLocation(imageDataUniqueKey,
                new GeoLocation(mLatLng.latitude, mLatLng.longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error == null) {
                            Timber.d("geolocation successfully uploaded");
                            uploadGeoLocationToPersonalInfo(imageDataUniqueKey);



                        } else {

                            Timber.d("geolocation upload failed");
                            //mFirebasePublicUploadHandler.onUploadPublicFail();


                        }
                    }
                });
    }


    private void uploadGeoLocationToPersonalInfo(String imageDataUniqueKey) {
        GeoFire geoFire = new GeoFire(mGeoFireReference
                .child(FirebaseContract.ImageGeofireDatabase.EACH_USER)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()));


        geoFire.setLocation(imageDataUniqueKey,
                new GeoLocation(mLatLng.latitude, mLatLng.longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error == null) {
                            Timber.d("geolocation successfully uploaded");

                            //mFirebasePublicUploadHandler.onUploadPublicSuccess();

                        } else {

                            Timber.d("geolocation upload failed");

                           //mFirebasePublicUploadHandler.onUploadPublicFail();


                        }
                    }
                });
    }


    /////////////////////////////////////////////////////////////////////////








    // Additional helper methods /////////////////////////////////////////////////////////
    private Bitmap getResizedBitmap(Bitmap image, int shortSide) {

        if (image.getWidth() <= image.getHeight()) {

            if (image.getWidth() > shortSide) {

                int width = image.getWidth();
                int height = image.getHeight();

                float bitmapRatio = (float) width / (float) height;

                width = shortSide;
                height = (int) (width / bitmapRatio);

                return Bitmap.createScaledBitmap(image, width, height, true);
            } else {
                return image;
            }
        }  else {

            if (image.getHeight() > shortSide) {

                int width = image.getWidth();
                int height = image.getHeight();

                float bitmapRatio = (float) width / (float) height;

                height = shortSide;
                width = (int) (height * bitmapRatio);

                return Bitmap.createScaledBitmap(image, width, height, true);


            } else {
                return image;
            }
        }

    }



    private String getUniqueName() {
        DatabaseReference reference = mImageUniqueNameReference.push();
        String longUniqueName = reference.toString();

        String[] arr = longUniqueName.split("name/");
        String uniqueName = arr[arr.length - 1];

        return uniqueName;
    }

}
