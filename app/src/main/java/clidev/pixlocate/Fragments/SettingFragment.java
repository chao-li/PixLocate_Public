package clidev.pixlocate.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.BuildConfig;
import clidev.pixlocate.R;
import clidev.pixlocate.RecyclerViewAdapters.AttribRecyclerViewAdapter;
import clidev.pixlocate.RecyclerViewAdapters.LicenseRecyclerViewAdapter;

public class SettingFragment extends Fragment{

    @BindView(R.id.setting_logout_button) Button mLogout;
    @BindView(R.id.setting_license_rv) RecyclerView mLicenseRv;
    @BindView(R.id.setting_icon_rv) RecyclerView mIconRv;
    @BindView(R.id.setting_adview) AdView mAdView;
    @BindView(R.id.setting_discord) ImageView mDiscord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        ButterKnife.bind(this, rootView);

        // implementing ad.
        if (BuildConfig.DEBUG) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("PLACE HOLDER")
                    .build();

            //mAdView.loadAd(adRequest);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }


        setLicenseRecyclerView();

        setAttribRecyclerView();


        setLogoutButtonAction();

        setDiscordButtonAction();



        return rootView;
    }

    private void setLicenseRecyclerView() {
        LicenseRecyclerViewAdapter licenseRecyclerViewAdapter = new LicenseRecyclerViewAdapter();
        mLicenseRv.setAdapter(licenseRecyclerViewAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mLicenseRv.setLayoutManager(linearLayoutManager);

        mLicenseRv.setHasFixedSize(true);

    }

    private void setAttribRecyclerView() {
        AttribRecyclerViewAdapter attribRecyclerViewAdapter = new AttribRecyclerViewAdapter(getContext());
        mIconRv.setAdapter(attribRecyclerViewAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mIconRv.setLayoutManager(linearLayoutManager);

        mIconRv.setHasFixedSize(true);

    }


    private void setLogoutButtonAction() {

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Signing out?")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton("Yes I do", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseAuth.getInstance().signOut();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

    }

    private void setDiscordButtonAction() {
        mDiscord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://discord.gg/RGYeYT6";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

    }
}
