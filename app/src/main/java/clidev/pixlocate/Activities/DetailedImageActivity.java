package clidev.pixlocate.Activities;


import android.content.Intent;

import android.content.res.Configuration;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import clidev.pixlocate.BuildConfig;
import clidev.pixlocate.FirebaseDataObjects.FirebaseImageWithLocation;

import clidev.pixlocate.Keys.PutExtraKeys;

import clidev.pixlocate.R;
import clidev.pixlocate.Utilities.Utilities;
import timber.log.Timber;

public class DetailedImageActivity extends AppCompatActivity {


    private ImageView mDetailedImage;
    private TextView mUsernameTextView;
    private TextView mPrivacyModeText;
    private AdView mAdView;
    private TextView mDateText;


    private FirebaseImageWithLocation mFirebaseImageWithLocation;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // getting all relevant data about the picture
        // get objectId, username, bigImageUrl, imageUrl, latitude, longitude
        Intent intent = getIntent();
        mFirebaseImageWithLocation = intent.getParcelableExtra(PutExtraKeys.IMAGE_OBJECT);

        /*  maybe attach timestamp to the data in the database?
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("time_stamp_test");

        ref.setValue(mFirebaseImageWithLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.d("test tree created");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Timber.d("FAILLLLLED");
            }
        });

        Map map = new HashMap();
        map.put("time", ServerValue.TIMESTAMP);
        ref.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.d("test time created");
            }
        });
        */


        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_detailed_image);

            mDetailedImage = findViewById(R.id.detailed_image_view);
            mUsernameTextView = findViewById(R.id.usernameTextView);
            mPrivacyModeText = findViewById(R.id.privacyModeText);
            mAdView = findViewById(R.id.detail_adview);
            mDateText = findViewById(R.id.dateTextView);

            // adding ads
            if (BuildConfig.DEBUG) {
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice("PLACE HOLDER")
                        .build();
                //mAdView.loadAd(adRequest);
            } else {
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

            // load the photo
            loadPhoto();

            // show the privacy status of the photo
            showPrivacyStatus();

            // show upload date of the photo
            showUploadDate();


            Utilities.cancelToast(mToast);
            mToast.makeText(this, "Finer image may take a few seconds to load...", Toast.LENGTH_LONG).show();

        } else {
            setContentView(R.layout.activity_detailed_image_landscape);
            mDetailedImage = findViewById(R.id.detailed_image_view);

            // load the photo
            Glide.with(this)
                    .load(mFirebaseImageWithLocation.getImageUrl())
                    .thumbnail(Glide.with(this).load(mFirebaseImageWithLocation.getSmallImageUrl()))
                    .into(mDetailedImage);

        }
    }

    private void showPrivacyStatus() {
        if (mFirebaseImageWithLocation.getPrivatePhoto() == true) {
            mPrivacyModeText.setText("Private");
            mPrivacyModeText.setTextColor(Color.parseColor("#FF4081"));
        } else {
            mPrivacyModeText.setText("Public");
            mPrivacyModeText.setTextColor(Color.parseColor("#3F51B5"));
        }
    }

    private void loadPhoto() {
        // load the photo
        Glide.with(this)
                .load(mFirebaseImageWithLocation.getImageUrl())
                .thumbnail(Glide.with(this).load(mFirebaseImageWithLocation.getSmallImageUrl()))
                .into(mDetailedImage);

        mUsernameTextView.setText(mFirebaseImageWithLocation.getUsername());
    }

    private void showUploadDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = formatter.format(new Date(mFirebaseImageWithLocation.getTimeStamp()));


        String[] dateSplit = dateString.split("/");
        String day = dateSplit[0];
        String month = dateSplit[1];
        String year = dateSplit[2];

        String monthInWord;

        switch (month) {
            case "01":
                monthInWord = "January";
                break;

            case "02":
                monthInWord = "February";
                break;

            case "03":
                monthInWord = "March";
                break;

            case "04":
                monthInWord = "April";
                break;

            case "05":
                monthInWord = "May";
                break;

            case "06":
                monthInWord = "June";
                break;

            case "07":
                monthInWord = "July";
                break;

            case "08":
                monthInWord = "August";
                break;

            case "09":
                monthInWord = "September";
                break;

            case "10":
                monthInWord = "October";
                break;

            case "11":
                monthInWord = "November";
                break;

            case "12":
                monthInWord = "December";
                break;

                default:
                    monthInWord = "unknown";
                    break;


        }

        mDateText.setText(day + "-" + monthInWord + "-" + year);

    }






}
