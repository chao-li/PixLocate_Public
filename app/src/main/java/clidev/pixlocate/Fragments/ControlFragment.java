package clidev.pixlocate.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.R;
import timber.log.Timber;

public class ControlFragment extends Fragment implements View.OnClickListener{

    public static final String GALLERY = "GALLERY" ;
    public static final String PERSONAL = "PERSONAL";
    public static final String CAMERA = "CAMERA";
    public static final String EXPLORE = "EXPLORE";
    public static final String CONTROL = "CONTROL";
    public static final String GMAP = "GMAP";
    public static final String PERSONAL_EXPLORE = "PERSONAL_EXPLORE";
    public static final String SETTING = "SETTING";
    private static final String BUTTON_STATE = "BUTTON_STATE";


    private View mRootView;
    private String mButtonState;
    public OnControlPanelClicked mOnControlPanelClicked;


    @BindView(R.id.menuImageView) ImageView mSettingImageView;
    @BindView(R.id.galleryImageView) ImageView mGalleryImageView;
    @BindView(R.id.personalImageView) ImageView mPersonalImageView;
    @BindView(R.id.exploreImageView) ImageView mExploreImageView;




    public interface OnControlPanelClicked {
        void onControlPanelClicked(String controlOption);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(BUTTON_STATE, mButtonState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_control, container, false);
        mRootView = rootView;

        ButterKnife.bind(this, rootView);

        // setup initial state of the buttons
        if (savedInstanceState == null) {
            galleryClicked();
        } else {


            mButtonState = savedInstanceState.getString(BUTTON_STATE);
            if (mButtonState == GALLERY) {
                galleryClicked();
                Timber.d("gallery clicked");
            } else if (mButtonState == PERSONAL) {
                personalClicked();
                Timber.d("personal clicked");
            } else if (mButtonState == EXPLORE) {
                exploreClicked();
                Timber.d("explore clicked");
            } else if (mButtonState == SETTING) {
                menuClicked();
                Timber.d("setting clicked");
            }
        }

        // create the control panel clicked interface
        mOnControlPanelClicked = (OnControlPanelClicked) getContext();

        // setup control button.
        mSettingImageView.setOnClickListener(this);
        mGalleryImageView.setOnClickListener(this);
        mPersonalImageView.setOnClickListener(this);
        mExploreImageView.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.galleryImageView) {
            mOnControlPanelClicked.onControlPanelClicked(GALLERY);
            galleryClicked();
        } else if (view.getId() == R.id.personalImageView) {
            mOnControlPanelClicked.onControlPanelClicked(PERSONAL);
            personalClicked();
        } else if (view.getId() == R.id.exploreImageView) {
            mOnControlPanelClicked.onControlPanelClicked(EXPLORE);
            exploreClicked();
        } else if (view.getId() == R.id.menuImageView) {
            // menu not implemented yet
            mOnControlPanelClicked.onControlPanelClicked(SETTING);
            menuClicked();
        }

    }


    private void galleryClicked() {
        mButtonState = GALLERY;
        mGalleryImageView.setEnabled(false);
        mPersonalImageView.setEnabled(true);
        mExploreImageView.setEnabled(true);
        mSettingImageView.setEnabled(true);

    }

    private void personalClicked() {
        mButtonState = PERSONAL;
        mGalleryImageView.setEnabled(true);
        mPersonalImageView.setEnabled(false);
        mExploreImageView.setEnabled(true);
        mSettingImageView.setEnabled(true);
    }


    private void exploreClicked() {
        mButtonState = EXPLORE;
        mGalleryImageView.setEnabled(true);
        mPersonalImageView.setEnabled(true);
        mExploreImageView.setEnabled(false);
        mSettingImageView.setEnabled(true);
    }

    private void menuClicked() {
        mButtonState = SETTING;
        mGalleryImageView.setEnabled(true);
        mPersonalImageView.setEnabled(true);
        mExploreImageView.setEnabled(true);
        mSettingImageView.setEnabled(false);
    }









}
