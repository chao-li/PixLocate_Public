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

public class FirebaseUploadPrivateFast {

    private static final String IMAGE_BYTE = "IMAGE_BYTE";
    private static final String SMALL_IMAGE_BYTE = "SMALL_IMAGE_BYTE";

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

    // interface variable
    private FirebasePrivateUploadHandler mFirebasePrivateUploadHandler;


    // interface
    public interface FirebasePrivateUploadHandler{
        void onUploadPrivateSuccess();
        void onUploadPrivateFail();
    }


    // Constructor
    public FirebaseUploadPrivateFast(FirebasePrivateUploadHandler firebasePrivateUploadHandler) {
        mFirebasePrivateUploadHandler = firebasePrivateUploadHandler;
    }


    public void uploadImage(Bitmap bitmap, LatLng latlng) {
        mLatLng = latlng;

        if (mLatLng == null || bitmap == null) {
            mFirebasePrivateUploadHandler.onUploadPrivateFail();
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
                // upload image
                mByteArray = imageByteMap.get(IMAGE_BYTE);
                mSmallByteArray = imageByteMap.get(SMALL_IMAGE_BYTE);

                Timber.d("image compression success, uploading images");

                //  begin by uploading small image
                uploadPrivateSmallImage();



            } else {

                // Inform user that the upload has failed
                mFirebasePrivateUploadHandler.onUploadPrivateFail();
            }
        }
    }




    private void uploadPrivateSmallImage() {

        mSmallImageUniqueName = getUniqueName();

        mSmallImageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(FirebaseContract.ImageStorage.PRIVATE_SMALL_IMAGE_FOLDER)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mSmallImageUniqueName);


        mUploadSmallImageTask = mSmallImageReference.putBytes(mSmallByteArray);


        mUploadSmallImageTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Timber.d("Upload small image failed");

                mFirebasePrivateUploadHandler.onUploadPrivateFail();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ..

                Timber.d("Upload small image success");

                // if uploading small image is successful, upload big image
                uploadPrivateImage();


            }
        });
    }


    private void uploadPrivateImage() {
        // creating unique image name

        mImageUniqueName = getUniqueName();

        mImageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(FirebaseContract.ImageStorage.PRIVATE_IMAGE_FOLDER)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mImageUniqueName);

        mUploadImageTask = mImageReference.putBytes(mByteArray);

        mUploadImageTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Timber.d("Upload image failed");

                mFirebasePrivateUploadHandler.onUploadPrivateFail();


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
                mFirebasePrivateUploadHandler.onUploadPrivateSuccess();


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



                    // now get image download url
                    getImageDownloadUrl(mUploadImageTask, mImageReference);

                } else {

                    // Handle failures
                    // ...

                   //mFirebasePrivateUploadHandler.onUploadPrivateFail();

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

                   //mFirebasePrivateUploadHandler.onUploadPrivateFail();

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
                        //mFirebasePrivateUploadHandler.onUploadPrivateFail();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                   //mFirebasePrivateUploadHandler.onUploadPrivateFail();
                }
            });
        }



    }




    ////////////////////////////////////////////////////////////////////////////


    // firebase code for uploading image info to Database
    private void uploadImageInfoToDatabase() {
        // first get the image unique key

        final String imageDataUniqueKey = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(FirebaseContract.ImageDatabase.PRIVATE)
                .push().getKey();



        final FirebaseImageWithLocation firebaseImageWithLocation = new FirebaseImageWithLocation(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                true,
                mImageDownloadUrl,
                mSmallImageDownloadUrl,
                mImageUniqueName,
                mSmallImageUniqueName,
                mLatLng.latitude,
                mLatLng.longitude,
                imageDataUniqueKey,
                mImageUploadTime);



        // Uploading to all user database

        FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(imageDataUniqueKey)
                .setValue(firebaseImageWithLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Timber.d("private photo data uploaded to all user database");


                uploadGeoLocationInfo(imageDataUniqueKey);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.d("writing to database failed");

               //mFirebasePrivateUploadHandler.onUploadPrivateFail();
            }
        });



    }



    private void uploadGeoLocationInfo(String imageDataUniqueKey) {
        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.ImageGeofireDatabase.IMAGE_LOCATION)
                .child(FirebaseContract.ImageGeofireDatabase.EACH_USER)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()));


        geoFire.setLocation(imageDataUniqueKey,
                new GeoLocation(mLatLng.latitude, mLatLng.longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error == null) {
                            Timber.d("geolocation successfully uploaded");

                           //mFirebasePrivateUploadHandler.onUploadPrivateSuccess();

                        } else {

                            Timber.d("geolocation upload failed");

                          //mFirebasePrivateUploadHandler.onUploadPrivateFail();
                        }
                    }
                });
    }







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
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.IMAGE_UNIQUE_NAME)
                .push();

        String longUniqueName = reference.toString();

        String[] arr = longUniqueName.split("name/");
        String uniqueName = arr[arr.length - 1];

        return uniqueName;
    }


}
