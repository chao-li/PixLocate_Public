package clidev.pixlocate.FirebaseUtilities.Download;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;
import clidev.pixlocate.FirebaseUtilities.FirebaseContract;

public class FirebaseDownloadFromPersonal {


    private static final int NUM_INITIAL_LOAD = 18;
    public static final int NUM_SCROLL_LOAD = 19;

    private PersonalPhotoHandler mPersonalPhotoHandler;


    // interface
    public interface PersonalPhotoHandler{
        void onPersonalPhotoFound(FirebaseImageWithLocation firebaseImageWithLocation);
        void onPersonalPhotoNotFound();
        void onScrollPhotoFound(FirebaseImageWithLocation firebaseImageWithLocation);

    }


    // Constructor
    public FirebaseDownloadFromPersonal(PersonalPhotoHandler personalPhotoHandler) {
        mPersonalPhotoHandler =  personalPhotoHandler;
    }



    // methods
    public void queryPersonalDatabase() {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        reference.orderByKey().limitToLast(NUM_INITIAL_LOAD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {

                        FirebaseImageWithLocation firebaseImageWithLocation =
                                singleSnapShot.getValue(FirebaseImageWithLocation.class);

                        mPersonalPhotoHandler.onPersonalPhotoFound(firebaseImageWithLocation);

                    }
                } else {
                    mPersonalPhotoHandler.onPersonalPhotoNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mPersonalPhotoHandler.onPersonalPhotoNotFound();
            }
        });

    }

    public void scrollQueryPersonalDatabase(String key) {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child(FirebaseContract.ImageDatabase.ALL_USER_IMAGES)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        reference.orderByKey().limitToLast(NUM_SCROLL_LOAD).endAt(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {

                        FirebaseImageWithLocation firebaseImageWithLocation =
                                singleSnapShot.getValue(FirebaseImageWithLocation.class);

                        mPersonalPhotoHandler.onScrollPhotoFound(firebaseImageWithLocation);

                    }
                } else {
                    mPersonalPhotoHandler.onPersonalPhotoNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mPersonalPhotoHandler.onPersonalPhotoNotFound();
            }
        });


    }


}
