package clidev.pixlocate.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.FirebaseDataObjects.FirebaseUserDetail;
import clidev.pixlocate.FirebaseUtilities.FirebaseAuthUtilities;
import clidev.pixlocate.FirebaseUtilities.FirebaseContract;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.R;
import clidev.pixlocate.Utilities.NetworkUtilities;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class WelcomeActivity extends AppCompatActivity implements FirebaseAuthUtilities.FirebaseAuthHandler {

    private FirebaseAuthUtilities mFirebaseAuthUtilities;
    private Toast mToast;

    @BindView(R.id.welcome_progress)
    ProgressBar mProgressBar;
    @BindView(R.id.welcome_progress_text)
    TextView mProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ButterKnife.bind(this);

        // Starting firebase authentication
        if (NetworkUtilities.isNetworkAvailable(this) == false) {
            NetworkUtilities.alertNetworkNotAvailable(this);
        }
        mFirebaseAuthUtilities = new FirebaseAuthUtilities(this);
        mFirebaseAuthUtilities.initializeAuthFields();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuthUtilities.attachFirebaseAuthListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuthUtilities.detachFirebaseAuthListener();
    }


    // Firebase authentication call back
    @Override
    public void onUserSignedIn() {
        if (NetworkUtilities.isNetworkAvailable(this)) {
            checkIfShouldRegisterUser();
        } else {
            NetworkUtilities.alertNetworkNotAvailable(this);
            FirebaseAuth.getInstance().signOut(); // sign user out.
        }
    }


    // Register user /////////////////////////////////////////////////////////////////
    private void checkIfShouldRegisterUser() {
        showProgressBar();
        mProgressText.setText("Checking user credential...");

        Utilities.cancelToast(mToast);
        mToast.makeText(this, "Verifying user details....", Toast.LENGTH_LONG).show();

        // maybe save the current logged in user's name?
        String userUniqueId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String userRank = "normal";
        String userTitle = "";
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        final FirebaseUserDetail userDetail = new FirebaseUserDetail(userUniqueId, username, userRank, userTitle, userEmail);

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child(FirebaseContract.User.USER_DETAILS)
                .child(userUniqueId);

        //registerUser(userRef, userDetail);


        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren() == false) {

                    Timber.d("User not registered, registering now");

                    registerUser(userRef, userDetail);
                } else {

                    Timber.d("user is registered");
                    redirectToMainApp();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utilities.cancelToast(mToast);
                mToast.makeText(WelcomeActivity.this,
                        "Error occurred when verifying user detail... logging out now", Toast.LENGTH_LONG)
                        .show();

                hideProgressBar();

                FirebaseAuth.getInstance().signOut();

            }
        });



    }



    private void registerUser(DatabaseReference userRef, FirebaseUserDetail userDetail) {
        mProgressText.setText("New user, registering credential...");

        userRef.setValue(userDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                redirectToMainApp();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Utilities.cancelToast(mToast);
                mToast.makeText(WelcomeActivity.this,
                        "Error occurred while registering user detail... logging out now....", Toast.LENGTH_LONG)
                        .show();

                FirebaseAuth.getInstance().signOut();
            }
        });
    }


    private void redirectToMainApp() {
        hideProgressBar();

        //cancelToast(mToast);
        //mToast.makeText(this, "User validated, re-directing to app...", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(WelcomeActivity.this, MainAppActivity.class);
        startActivity(intent);
    }




    @Override
    public void onUserSignedOut() {
        // check for network access
        if (NetworkUtilities.isNetworkAvailable(this)) {

            /*
            // check if this version is allowed
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("version_check");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String version = dataSnapshot.getValue(String.class);
                        Timber.d("version is: " + version);

                        if (version.matches("debug version")) {

                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setIsSmartLockEnabled(false)
                                            .setAvailableProviders(Arrays.asList(
                                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                                            .build(),
                                    RequestCodes.RC_SIGN_IN);

                        } else {
                            Utilities.cancelToast(mToast);
                            mToast.makeText(WelcomeActivity.this, "YOUR VERSION HAS EXPIRED", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Utilities.cancelToast(mToast);
                        mToast.makeText(WelcomeActivity.this, "YOUR VERSION HAS EXPIRED", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Utilities.cancelToast(mToast);
                    mToast.makeText(WelcomeActivity.this, "YOUR VERSION HAS EXPIRED", Toast.LENGTH_SHORT).show();
                }
            });
            */

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RequestCodes.RC_SIGN_IN);

        } else {
            NetworkUtilities.alertNetworkNotAvailable(this);
        }
    }



    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressText.setVisibility(View.INVISIBLE);
    }

}
