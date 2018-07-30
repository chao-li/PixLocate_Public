package clidev.pixlocate.Activities;



import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.MobileAds;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.Arrays;
import java.util.ResourceBundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.FirebaseUtilities.FirebaseAuthUtilities;
import clidev.pixlocate.Fragments.ControlFragment;
import clidev.pixlocate.Fragments.ExploreFragment;
import clidev.pixlocate.Fragments.GalleryMapFragment;
import clidev.pixlocate.Fragments.PersonalMapFragment;
import clidev.pixlocate.Fragments.SettingFragment;
import clidev.pixlocate.R;
import clidev.pixlocate.Keys.RequestCodes;
import clidev.pixlocate.Utilities.LastKnownLocationUtilities;
import timber.log.Timber;

public class MainAppActivity extends AppCompatActivity
        implements ControlFragment.OnControlPanelClicked,
        FirebaseAuthUtilities.FirebaseAuthHandler,
        GalleryMapFragment.GalleryFloatButtonHandler,
        PersonalMapFragment.PersonalFloatButtonHandler{


    public static final String LOCATION_KEY = "LOCATION_KEY";
    public static final String SCREEN_KEY = "SCREEN_KEY";
    private static final String SCREEN_STATE = "SCREEN_STATE";

    // firebase utilities
    FirebaseAuthUtilities mFirebaseAuthUtilities;


    // utilities
    private LastKnownLocationUtilities mLastKnownLocationUtilities;

    // fragments
    private FragmentManager mFragmentManager;
    private Fragment mGalleryMapFragment;
    private Fragment mPersonalMapFragment;
    private Fragment mControlFragment;
    private Fragment mExploreFragment;
    private Fragment mSettingFragment;

    // fields
    private StyleableToast mToast;
    private String mCurrentScreen;



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SCREEN_STATE, mCurrentScreen);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestCodes.FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // can later be removed
                } else {

                }
            }

        }
    }


    ////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        // initializing admob
        MobileAds.initialize(this, "ca-app-pub-4730140081690747~4535153370");

        // Starting Firebase utilities
        mFirebaseAuthUtilities = new FirebaseAuthUtilities(this);
        mFirebaseAuthUtilities.initializeAuthFields();


        ButterKnife.bind(this);


        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {

            // Setup Control Fragment
            mControlFragment = new ControlFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.control_fragment_container, mControlFragment, ControlFragment.CONTROL)
                    .commit();

            // open the default screen, which is the map and gallery fragments.
            mGalleryMapFragment = new GalleryMapFragment();

            mFragmentManager.beginTransaction()
                    .add(R.id.general_fragment_container, mGalleryMapFragment, ControlFragment.GALLERY)
                    .addToBackStack(ControlFragment.GALLERY)
                    .show(mGalleryMapFragment)
                    .commit();



        } else { // if phone is rotated or app is left in background for a long time.
            mCurrentScreen = savedInstanceState.getString(SCREEN_STATE);


            mControlFragment = getSupportFragmentManager().findFragmentByTag(ControlFragment.CONTROL);
            mGalleryMapFragment = getSupportFragmentManager().findFragmentByTag(ControlFragment.GALLERY);
            mPersonalMapFragment = getSupportFragmentManager().findFragmentByTag(ControlFragment.PERSONAL);
            mExploreFragment = getSupportFragmentManager().findFragmentByTag(ControlFragment.EXPLORE);
            mSettingFragment = getSupportFragmentManager().findFragmentByTag(ControlFragment.SETTING);

        }


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



    // Firebase authentication logic ///////////////////////////////////////////////////
    @Override
    public void onUserSignedIn() {

    }

    @Override
    public void onUserSignedOut() {
        // re-divert user to welcome screen.
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "you have logged in.", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "you have logged out.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////




    // GALLERY VIEW ///////////////////////////////////////
    private void replaceAllFragmentsForGalleryView() {
        // Creating Fragments
        mCurrentScreen = ControlFragment.GALLERY;


        if (mGalleryMapFragment == null) {
            mGalleryMapFragment = new GalleryMapFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.general_fragment_container, mGalleryMapFragment, ControlFragment.GALLERY)
                    .addToBackStack(ControlFragment.GALLERY)
                    .show(mGalleryMapFragment)
                    .commit();

        } else {
            mFragmentManager.beginTransaction()
                    .show(mGalleryMapFragment)
                    .addToBackStack(ControlFragment.GALLERY)
                    .commit();
        }


        hidePersonalFragment();

        hideExploreFragment();

        deleteSettingFragment();

    }



    // PERSONAL VIEW ////////////

    private void replaceAllFragmentsForPersonalView() {
        // define current fragment status
        mCurrentScreen = ControlFragment.PERSONAL;


        if (mPersonalMapFragment == null) {
            mPersonalMapFragment = new PersonalMapFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.general_fragment_container, mPersonalMapFragment, ControlFragment.PERSONAL)
                    .addToBackStack(ControlFragment.PERSONAL)
                    .show(mPersonalMapFragment)
                    .commit();
        } else {
            mFragmentManager.beginTransaction()
                    .show(mPersonalMapFragment)
                    .addToBackStack(ControlFragment.PERSONAL)
                    .commit();
        }

        hideGalleryFragment();

        hideExploreFragment();

        deleteSettingFragment();
    }


    // EXPLORE VIEW
    private void replaceAllFragmentsForExploreView() {
        mCurrentScreen = ControlFragment.EXPLORE;


        if (mExploreFragment == null) {
            mExploreFragment = new ExploreFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.general_fragment_container, mExploreFragment, ControlFragment.EXPLORE)
                    .addToBackStack(ControlFragment.EXPLORE)
                    .show(mExploreFragment)
                    .commit();
        } else {
            mFragmentManager.beginTransaction()
                    .show(mExploreFragment)
                    .addToBackStack(ControlFragment.EXPLORE)
                    .commit();
        }

        hideGalleryFragment();

        hidePersonalFragment();

        deleteSettingFragment();
    }

    // SETTING VIEW
    // Note: we will not be keeping setting fragment in the back stack. As data don't need to be loaded from the internet
    private void replaceAllFragmentsForSettingView() {
        mCurrentScreen = ControlFragment.SETTING;

        if (mSettingFragment == null) {
            mSettingFragment = new SettingFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.general_fragment_container, mSettingFragment, ControlFragment.SETTING)
                    .show(mSettingFragment)
                    .commit();
        } else {
            // if not null, remove and clear the setting fragment, then reboot it.
            mFragmentManager.beginTransaction().remove(mSettingFragment).commit();
            mSettingFragment = null;
            replaceAllFragmentsForSettingView();
        }


        hideGalleryFragment();

        hidePersonalFragment();

        hideExploreFragment();

    }

    private void hideGalleryFragment() {
        if (mGalleryMapFragment != null) {
            mFragmentManager.beginTransaction()
                    .hide(mGalleryMapFragment)
                    .commit();
        }
    }

    private void deleteSettingFragment() {
        if (mSettingFragment != null) { // deleting setting fragment, not keeping in back stack.
            mFragmentManager.beginTransaction().remove(mSettingFragment).commit();
            mSettingFragment = null;
        }
    }

    private void hideExploreFragment() {
        if (mExploreFragment != null) {
            mFragmentManager.beginTransaction()
                    .hide(mExploreFragment)
                    .commit();
        }
    }

    private void hidePersonalFragment() {
        if (mPersonalMapFragment != null) {
            mFragmentManager.beginTransaction()
                    .hide(mPersonalMapFragment)
                    .commit();
        }
    }


    ////////////////////////////////////////////////////////////////



    // BUTTON CONTROL ////////////////////////////////////////////////////////////
    @Override
    public void onControlPanelClicked(String controlOption) {

        if (controlOption == ControlFragment.GALLERY) {
            // User have chose to load the gallery screen
            if (mCurrentScreen == ControlFragment.GALLERY) {
                // we are already in gallery screen, proceed to do nothing.
                Timber.d("Already in gallery screen, proceed to do nothing");
            } else {
                // close all un-needed fragments
                //closeAllFragments();

                // load map and gallery fragments
                replaceAllFragmentsForGalleryView();
            }

        } else if (controlOption == ControlFragment.PERSONAL) {
            // User have chose to load the personal gallery
            if (mCurrentScreen == ControlFragment.PERSONAL) {
                // we are already in personal gallery screen, proceed to do nothing.
                Timber.d("Already in personal screen, proceed to do nothing");
            } else {
                // close all un-needed fragments
                //closeAllFragments();

                // load PersonalFragments.
                replaceAllFragmentsForPersonalView();
            }

        } else if (controlOption == ControlFragment.EXPLORE) {
            // User have chose to load the settings screen
            if (mCurrentScreen == ControlFragment.EXPLORE) {
                // already in this screen, do nothing
                Timber.d("Already in EXPLORATION screen, do nothing");
            } else {
                // close all un-needed fragments
                //closeAllFragments();

                // load exploreFragments.
                replaceAllFragmentsForExploreView();
            }
        } else if (controlOption == ControlFragment.SETTING) {
            if (mCurrentScreen == ControlFragment.SETTING) {
                Timber.d("Already in SETTING screen, do nothing");
            } else {
                replaceAllFragmentsForSettingView();
            }
        }
    }


    private void closeAllFragments() {
        // close gallery fragment
        if (mGalleryMapFragment != null) {
            mFragmentManager.beginTransaction().remove(mGalleryMapFragment).commit();
        }

        // close personal fragment
        if (mPersonalMapFragment != null) {
            mFragmentManager.beginTransaction().remove(mPersonalMapFragment).commit();
        }

        // close explore Fragment
        if (mExploreFragment != null) {
            mFragmentManager.beginTransaction().remove(mExploreFragment).commit();
        }

    }


    ////////////////////////////////////////////////////////////////


    // FLOATING ACTION BUTTONS
    @Override
    public void onGalleryRefreshRequested() {
        if (mGalleryMapFragment != null) {
            mFragmentManager.beginTransaction().remove(mGalleryMapFragment).commit();

            mGalleryMapFragment = null;

            replaceAllFragmentsForGalleryView();
        }
    }


    @Override
    public void onPersonalRefreshRequeted() {
        if (mPersonalMapFragment != null) {
            mFragmentManager.beginTransaction().remove(mPersonalMapFragment).commit();

            mPersonalMapFragment = null;

            replaceAllFragmentsForPersonalView();
        }
    }



    ///////////////////////////////////////////////////////////////


    @Override
    public void onBackPressed() {
        // show dialog box, asking if people want to exit app.
        new AlertDialog.Builder(MainAppActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit app?")
                .setMessage("Do you want to close and exit this app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainAppActivity.this.moveTaskToBack(true);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }



}
