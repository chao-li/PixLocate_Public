package clidev.pixlocate.FirebaseUtilities;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class FirebaseAuthUtilities {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuthHandler mFirebaseAuthHandler;


    public interface FirebaseAuthHandler {
        void onUserSignedIn();
        void onUserSignedOut();
    }


    public FirebaseAuthUtilities (FirebaseAuthHandler firebaseAuthHandler) {
        mFirebaseAuthHandler = firebaseAuthHandler;
    }


    public void initializeAuthFields() {
        // initializse firebas auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        // create the state listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    //  logic for signed in.
                    mFirebaseAuthHandler.onUserSignedIn();


                } else {
                    //  Signout cleanup logic.
                    mFirebaseAuthHandler.onUserSignedOut();
                }
            }

        };
    }

    public void attachFirebaseAuthListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

    }

    public void detachFirebaseAuthListener() {
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

}
