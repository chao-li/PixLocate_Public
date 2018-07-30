package clidev.pixlocate.RecyclerViewAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import clidev.pixlocate.Licensing.LicenseObject;
import clidev.pixlocate.Licensing.LicenseObjectListCreator;
import clidev.pixlocate.R;

public class LicenseRecyclerViewAdapter extends RecyclerView.Adapter<LicenseRecyclerViewAdapter.LicenseViewHolder>{

    private List<LicenseObject> mLicenseList;

    public LicenseRecyclerViewAdapter() {
        mLicenseList = LicenseObjectListCreator.compileLicenseObjectList();
    }


    @NonNull
    @Override
    public LicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_license_list, parent, false);

        LicenseViewHolder viewHolder = new LicenseViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LicenseViewHolder holder, int position) {

        holder.mLibraryTv.setText(mLicenseList.get(position).getLibrary());
        holder.mLicenseTv.setText(mLicenseList.get(position).getLicense());

    }

    @Override
    public int getItemCount() {
        return mLicenseList.size();
    }




    // view holder
    public class LicenseViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.license_library) TextView mLibraryTv;
        @BindView(R.id.license_license) TextView mLicenseTv;

        public LicenseViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
